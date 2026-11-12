package com.mhamauto.db;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HexFormat;

/** V002 checksum tracked alongside existing V001. MySQL DDL is not rollback-atomic. */
public final class IdentityMigration {
    private IdentityMigration() { }
    public static String apply(Connection c,Path file) throws Exception {
        byte[] data=Files.readAllBytes(file);
        String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data));
        try(PreparedStatement q=c.prepareStatement("SELECT sha256 FROM schema_migrations WHERE version='V002'")) {
            try(ResultSet rs=q.executeQuery()) {
                if(rs.next()) {
                    if(!sha.equals(rs.getString(1))) throw new IllegalStateException("V002 migration checksum changed; do not edit applied migrations");
                    return "V002 already applied (checksum matches)";
                }
            }
        }
        StringBuilder code=new StringBuilder();
        for(String line:new String(data,StandardCharsets.UTF_8).split("\\R")) {
            if(!line.stripLeading().startsWith("--")) code.append(line).append('\n');
        }
        for(String statement:code.toString().split(";")) {
            if(statement.isBlank()) continue;
            try(Statement s=c.createStatement()) {s.execute(statement);}
        }
        try(PreparedStatement ps=c.prepareStatement("INSERT INTO schema_migrations(version,sha256) VALUES ('V002',?)")) {
            ps.setString(1,sha);ps.executeUpdate();
        }
        return "V002 applied";
    }
}
