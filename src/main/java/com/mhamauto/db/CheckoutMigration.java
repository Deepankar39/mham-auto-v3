package com.mhamauto.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.sql.*;
import java.util.HexFormat;

/** Append-only V004 MySQL migration. Previous migrations are unchanged and required. */
public final class CheckoutMigration {
    private CheckoutMigration(){ }
    public static String apply(Connection c,Path path) throws Exception {
        byte[] content=Files.readAllBytes(path);
        String sha=HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content));
        boolean previous=false;
        try(PreparedStatement p=c.prepareStatement("SELECT version,sha256 FROM schema_migrations WHERE version IN ('V003','V004') ORDER BY version")){
            try(ResultSet r=p.executeQuery()){
                while(r.next()){
                    if("V003".equals(r.getString(1))) previous=true;
                    if("V004".equals(r.getString(1))){
                        if(!previous)throw new IllegalStateException("V003 missing; run Phase 5 first");
                        if(!sha.equals(r.getString(2)))throw new IllegalStateException("V004 migration checksum mismatch");
                        return "V004 already applied (checksum matches)";
                    }
                }
            }
        }
        if(!previous)throw new IllegalStateException("Phase 5 V003 migration must be present first");
        StringBuilder sql=new StringBuilder();
        for(String line:new String(content,StandardCharsets.UTF_8).split("\\R")) if(!line.stripLeading().startsWith("--"))sql.append(line).append('\n');
        // MySQL DDL commits automatically; verify DB backup before using this migrator.
        for(String statement:sql.toString().split(";"))if(!statement.isBlank())try(Statement s=c.createStatement()){s.execute(statement);}
        try(PreparedStatement p=c.prepareStatement("INSERT INTO schema_migrations(version,sha256) VALUES('V004',?)")){
            p.setString(1,sha);p.executeUpdate();
        }
        return "V004 applied";
    }
}
