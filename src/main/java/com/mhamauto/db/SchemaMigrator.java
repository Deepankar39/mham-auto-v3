package com.mhamauto.db;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;

/** V001 migration. MySQL DDL auto-commits; every CREATE uses IF NOT EXISTS for safe retry. */
public final class SchemaMigrator {
    private SchemaMigrator() { }
    public static String apply(Connection connection, Path file) throws Exception {
        byte[] bytes=Files.readAllBytes(file);
        String sql=new String(bytes,StandardCharsets.UTF_8);
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        try (Statement stmt=connection.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS schema_migrations (" +
                "version VARCHAR(40) NOT NULL PRIMARY KEY, sha256 CHAR(64) NOT NULL," +
                "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP) ENGINE=InnoDB");
        }
        try (PreparedStatement ps=connection.prepareStatement("SELECT sha256 FROM schema_migrations WHERE version=?")) {
            ps.setString(1,"V001");
            try (ResultSet rs=ps.executeQuery()) {
                if (rs.next()) {
                    if (!rs.getString(1).equals(hash)) throw new IllegalStateException("V001 script changed after migration; do not edit deployed migrations");
                    return "V001 already applied (checksum matches)";
                }
            }
        }
        // V001 has no stored procedures or quoted semicolons. Keep future migrations separate.
        StringBuilder clean=new StringBuilder();
        for(String line:sql.split("\\R")) {
            if (!line.stripLeading().startsWith("--")) clean.append(line).append('\n');
        }
        for(String chunk:clean.toString().split(";")) {
            if (chunk.isBlank()) continue;
            try (Statement stmt=connection.createStatement()) { stmt.execute(chunk); }
        }
        try (PreparedStatement ps=connection.prepareStatement("INSERT INTO schema_migrations(version,sha256) VALUES (?,?)")) {
            ps.setString(1,"V001"); ps.setString(2,hash); ps.executeUpdate();
        }
        return "V001 applied";
    }
}
