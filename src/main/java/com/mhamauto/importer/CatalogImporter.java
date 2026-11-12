package com.mhamauto.importer;

import com.mhamauto.dao.CatalogImportDao;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.util.HexFormat;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded-memory import with per-row savepoints and periodic transaction commits. */
public final class CatalogImporter {
    private CatalogImporter() { }
    public record Results(long jobId,long rows,long successful,long errors,long warnings) { }

    public static Results dryRun(Path file, String currency) throws IOException {
        AtomicLong seen=new AtomicLong(),valid=new AtomicLong(),errors=new AtomicLong(),warnings=new AtomicLong();
        ShopifyCsvReader.forEach(file,r -> {
            seen.incrementAndGet();
            try {
                r.validate(currency);
                valid.incrementAndGet();
                warnings.addAndGet(r.warningMessages().size());
            } catch (IllegalArgumentException ex) {
                errors.incrementAndGet();
                if (errors.get()<=10) System.err.println("Record " + r.recordNumber() + ": " + ex.getMessage());
            }
        });
        return new Results(0,seen.get(),valid.get(),errors.get(),warnings.get());
    }

    public static Results importFile(Connection conn,Path csv,String currency,int batchSize) throws Exception {
        if (batchSize<1 || batchSize>1000) throw new IllegalArgumentException("Batch size must be 1..1000");
        if (!Files.isRegularFile(csv)) throw new IllegalArgumentException("CSV file does not exist");
        String digest=fileSha(csv);
        long jobId;
        try (PreparedStatement ps=conn.prepareStatement("INSERT INTO import_jobs(source_name,source_sha256,source_currency) VALUES (?,?,?)",PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1,csv.getFileName().toString()); ps.setString(2,digest);
            ps.setString(3,"KWD".equals(currency)?currency:null);
            ps.executeUpdate();
            try (ResultSet r=ps.getGeneratedKeys()) { if (!r.next()) throw new IllegalStateException("Import job ID missing"); jobId=r.getLong(1); }
        }
        // Commit job creation before batch import, so partial failures remain visible.
        long[] counter = new long[4]; // seen, imported, errors, warnings
        boolean priorAutoCommit=conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (CatalogImportDao dao=new CatalogImportDao(conn,jobId,currency)) {
            try {
                ShopifyCsvReader.forEach(csv,r -> {
                    counter[0]++;
                    Savepoint savepoint;
                    try { savepoint=conn.setSavepoint(); }
                    catch (Exception ex) { throw new ImportFatalException(ex); }
                    try {
                        dao.accept(r);
                        for (String warning : r.warningMessages()) {
                            dao.issue(r.recordNumber(),"WARN",warning);
                        }
                        counter[3] += r.warningMessages().size();
                        counter[1]++;
                    } catch (Exception ex) {
                        try {
                            conn.rollback(savepoint);
                            String message=ex instanceof java.sql.SQLException ? "SQL constraint or database error (see import mapping/schema)" : ex.getMessage();
                            dao.issue(r.recordNumber(),"ERROR", message==null ? "Unknown row error" : message);
                            counter[2]++;
                            if (counter[2]<=10) System.err.println("Rejected record " + r.recordNumber() + ": " + message);
                        } catch (Exception persistError) { throw new ImportFatalException(persistError); }
                    }
                    if (counter[0]%batchSize==0) {
                        try { updateProgress(conn,jobId,counter,false); conn.commit(); }
                        catch (Exception ex) { throw new ImportFatalException(ex); }
                    }
                });
                updateProgress(conn,jobId,counter,true);
                conn.commit();
            } catch (Exception fatal) {
                conn.rollback();
                // Keep the job visible even if a malformed CSV or DB connection aborts the import.
                conn.setAutoCommit(true);
                try (PreparedStatement ps=conn.prepareStatement("UPDATE import_jobs SET status='FAILED', finished_at=NOW() WHERE id=?")) {
                    ps.setLong(1,jobId); ps.executeUpdate();
                } catch (Exception ignored) { /* An unavailable DB may prevent status update. */ }
                throw fatal;
            }
        } finally {
            conn.setAutoCommit(priorAutoCommit);
        }
        return new Results(jobId,counter[0],counter[1],counter[2],counter[3]);
    }

    private static void updateProgress(Connection conn,long job,long[] counters,boolean finished) throws Exception {
        String query="UPDATE import_jobs SET rows_seen=?,rows_imported=?,errors_seen=?,warnings_seen=?,status=?,finished_at=" +
                     (finished?"NOW()":"NULL") + " WHERE id=?";
        try (PreparedStatement ps=conn.prepareStatement(query)) {
            ps.setLong(1,counters[0]); ps.setLong(2,counters[1]);ps.setLong(3,counters[2]);ps.setLong(4,counters[3]);
            ps.setString(5,finished?(counters[2]==0?"COMPLETED":"PARTIAL"):"RUNNING");ps.setLong(6,job);ps.executeUpdate();
        }
    }

    private static String fileSha(Path file) throws Exception {
        MessageDigest sha=MessageDigest.getInstance("SHA-256");
        try (var in=Files.newInputStream(file)) {
            byte[] bytes=new byte[65536]; int n;
            while ((n=in.read(bytes))!=-1) sha.update(bytes,0,n);
        }
        return HexFormat.of().formatHex(sha.digest());
    }
    private static final class ImportFatalException extends RuntimeException {
        ImportFatalException(Throwable cause) { super(cause); }
    }
}
