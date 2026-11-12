package com.mhamauto.cli;

import com.mhamauto.db.DbConfig;
import com.mhamauto.db.SchemaMigrator;
import com.mhamauto.importer.CatalogImporter;
import com.mhamauto.importer.DemoCsvGenerator;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

/** Java -cp target/classes;target/lib/* com.mhamauto.cli.CatalogCli <command> [--key=value]. */
public final class CatalogCli {
    private CatalogCli() { }
    public static void main(String[] args) throws Exception {
        if (args.length==0) { usage(); System.exit(2); }
        Map<String,String> opts=new HashMap<>();
        for(int i=1;i<args.length;i++) {
            int equal=args[i].indexOf('=');
            if (!args[i].startsWith("--") || equal<3) throw new IllegalArgumentException("Use --key=value (quote paths with spaces)");
            opts.put(args[i].substring(2,equal),args[i].substring(equal+1));
        }
        String currency=opts.getOrDefault("source-currency","UNKNOWN").toUpperCase(java.util.Locale.ROOT);
        if (!currency.equals("UNKNOWN") && !currency.equals("KWD")) {
            throw new IllegalArgumentException("--source-currency must be UNKNOWN or confirmed KWD; no conversion is implemented");
        }
        switch (args[0]) {
            case "generate-demo" -> {
                String output=opts.get("output");
                if (output==null || output.isBlank()) throw new IllegalArgumentException("Missing --output=path-to-csv");
                int count=Integer.parseInt(opts.getOrDefault("count","500"));
                DemoCsvGenerator.generate(requiredFile(opts),Path.of(output),count);
                System.out.println("Generated " + count + " explicitly synthetic rows; no image files or DB writes");
            }
            case "dry-run" -> {
                Path file=requiredFile(opts);
                var report=CatalogImporter.dryRun(file,currency);
                System.out.printf("DRY RUN: rows=%d valid=%d errors=%d warnings=%d (no database or image download)%n",
                        report.rows(),report.successful(),report.errors(),report.warnings());
                if (report.errors()>0) System.exit(1);
            }
            case "migrate", "import", "stats" -> {
                DbConfig config=DbConfig.fromEnvironment();
                try (Connection conn=DriverManager.getConnection(config.url(),config.username(),config.password())) {
                    if (args[0].equals("migrate")) {
                        System.out.println(SchemaMigrator.apply(conn,Path.of(opts.getOrDefault("schema","database/01_catalog_schema.sql"))));
                    } else if (args[0].equals("import")) {
                        int batch=Integer.parseInt(opts.getOrDefault("batch-size","250"));
                        var report=CatalogImporter.importFile(conn,requiredFile(opts),currency,batch);
                        System.out.printf("IMPORT #%d: rows=%d imported=%d errors=%d warnings=%d%n",
                          report.jobId(),report.rows(),report.successful(),report.errors(),report.warnings());
                        if (report.errors()>0) System.exit(1);
                    } else stats(conn);
                }
            }
            default -> { usage(); System.exit(2); }
        }
    }
    private static Path requiredFile(Map<String,String> opts) {
        String file=opts.get("file");
        if (file==null || file.isBlank()) throw new IllegalArgumentException("Missing --file=path-to-csv");
        return Path.of(file);
    }
    private static void stats(Connection conn) throws Exception {
        for(String table : new String[]{"products","product_variants","media_assets","product_images","variant_images","inventory","import_jobs","import_errors"}) {
            try (Statement s=conn.createStatement(); ResultSet r=s.executeQuery("SELECT COUNT(*) FROM " + table)) {
                r.next(); System.out.printf("%-22s %d%n",table,r.getLong(1));
            }
        }
        try (Statement s=conn.createStatement();ResultSet r=s.executeQuery("SELECT COUNT(*) FROM product_variants WHERE retail_price_kwd IS NULL")) {
            r.next(); System.out.printf("%-22s %d%n","unverified_prices",r.getLong(1));
        }
    }
    private static void usage() {
        System.out.println("Commands: generate-demo --file=<source> --output=<generated-csv> [--count=500] | dry-run --file=<path> [--source-currency=UNKNOWN|KWD] | migrate | import --file=<path> [--batch-size=250] [--source-currency=UNKNOWN|KWD] | stats");
    }
}
