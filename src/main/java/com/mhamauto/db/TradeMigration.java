package com.mhamauto.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HexFormat;

/** Append-only V006; preserves all previous tables, roles, products, carts and orders. */
public final class TradeMigration {
    private TradeMigration() { }
    public static String apply(Connection c, Path file) throws Exception {
        byte[] source=Files.readAllBytes(file);
        String checksum=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(source));
        try(PreparedStatement p=c.prepareStatement("SELECT sha256 FROM schema_migrations WHERE version='V005'")) {
            try(ResultSet rs=p.executeQuery()){if(!rs.next())throw new IllegalStateException("V005 missing: apply Phase 8 first");}
        }
        try(PreparedStatement p=c.prepareStatement("SELECT sha256 FROM schema_migrations WHERE version='V006'")) {
            try(ResultSet rs=p.executeQuery()) {if(rs.next()) {
                if(!checksum.equals(rs.getString(1)))throw new IllegalStateException("V006 checksum differs from applied migration");
                return "V006 already applied (checksum matches)";
            }}
        }
        // This migration contains only CREATE TABLE DDL with no embedded semicolons.
        StringBuilder statements=new StringBuilder();
        for(String line:new String(source,StandardCharsets.UTF_8).split("\\R"))
            if(!line.stripLeading().startsWith("--"))statements.append(line).append('\n');
        for(String sql:statements.toString().split(";")) {
            if(!sql.isBlank())try(Statement stmt=c.createStatement()){stmt.execute(sql);}
        }
        try(PreparedStatement p=c.prepareStatement("INSERT INTO schema_migrations(version,sha256) VALUES('V006',?)")){
            p.setString(1,checksum);p.executeUpdate();
        }
        return "V006 applied";
    }
}
