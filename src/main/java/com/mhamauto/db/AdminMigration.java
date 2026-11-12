package com.mhamauto.db;

import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HexFormat;

/** V005 additive migration. Always back up MySQL first; MySQL DDL is non-transactional. */
public final class AdminMigration {
    private AdminMigration() { }
    public static String apply(Connection c,Path file) throws Exception {
        byte[] bytes=Files.readAllBytes(file);
        String hash=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(bytes));
        try(PreparedStatement p=c.prepareStatement("SELECT sha256 FROM schema_migrations WHERE version='V004'")) {
            try(ResultSet r=p.executeQuery()){if(!r.next())throw new IllegalStateException("V004 missing: complete Phase 6 first");}
        }
        try(PreparedStatement p=c.prepareStatement("SELECT sha256 FROM schema_migrations WHERE version='V005'")) {
            try(ResultSet r=p.executeQuery()){
                if(r.next()){
                    if(!hash.equals(r.getString(1)))throw new IllegalStateException("V005 checksum differs from deployed migration");
                    return "V005 already applied (checksum matches)";
                }
            }
        }
        StringBuilder sql=new StringBuilder();
        for(String line:new String(bytes,StandardCharsets.UTF_8).split("\\R"))if(!line.stripLeading().startsWith("--"))sql.append(line).append('\n');
        for(String statement:sql.toString().split(";"))if(!statement.isBlank())try(Statement s=c.createStatement()){s.execute(statement);}
        try(PreparedStatement p=c.prepareStatement("INSERT INTO schema_migrations(version,sha256) VALUES('V005',?)")){
            p.setString(1,hash);p.executeUpdate();
        }
        return "V005 applied";
    }
}
