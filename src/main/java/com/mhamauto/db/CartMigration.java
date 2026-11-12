package com.mhamauto.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;

/** Append-only V003 migration, checksum-checked against prior installed migrations. */
public final class CartMigration {
    private CartMigration() { }
    public static String apply(Connection c, Path path) throws Exception {
        byte[] data=Files.readAllBytes(path);
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        try (PreparedStatement p=c.prepareStatement("SELECT version,sha256 FROM schema_migrations WHERE version IN ('V002','V003') ORDER BY version")) {
            boolean previous=false;
            try (ResultSet r=p.executeQuery()) {
                while(r.next()) {
                    if("V002".equals(r.getString(1))) previous=true;
                    if("V003".equals(r.getString(1))) {
                        if(!previous) throw new IllegalStateException("V002 missing. Apply Phase 4 migration first");
                        if(!hash.equals(r.getString(2))) throw new IllegalStateException("V003 checksum mismatch: original migration was modified");
                        return "V003 already applied (matching checksum)";
                    }
                }
            }
            if(!previous) throw new IllegalStateException("V002 missing. Apply Phase 4 identity migration first");
        }
        StringBuilder sql=new StringBuilder();
        for (String line:new String(data,StandardCharsets.UTF_8).split("\\R"))
            if(!line.stripLeading().startsWith("--")) sql.append(line).append('\n');
        // Only straightforward CREATE TABLE statements; MySQL DDL auto commits.
        for(String statement:sql.toString().split(";")) {
            if(statement.isBlank()) continue;
            try(Statement s=c.createStatement()) {s.execute(statement);}
        }
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO schema_migrations(version,sha256) VALUES('V003',?)")) {
            ps.setString(1,hash); ps.executeUpdate();
        }
        return "V003 applied";
    }
}
