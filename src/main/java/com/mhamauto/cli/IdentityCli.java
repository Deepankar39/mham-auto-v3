package com.mhamauto.cli;

import com.mhamauto.auth.AuthDao;
import com.mhamauto.auth.IdentityRules;
import com.mhamauto.db.DbPool;
import com.mhamauto.db.IdentityMigration;
import java.nio.file.Path;
import java.sql.Connection;

/** Commands run locally with database credentials. Never deploy an HTTP admin-bootstrap endpoint. */
public final class IdentityCli {
    private IdentityCli() { }
    public static void main(String[] args) throws Exception {
        if(args.length==1 && args[0].equals("migrate")) {
            try(Connection c=DbPool.getConnection()) {
                System.out.println(IdentityMigration.apply(c,Path.of("database","03_identity_schema.sql")));
            }
        } else if(args.length==1 && args[0].equals("status")) {
            try(Connection c=DbPool.getConnection()) {
                try(var ps=c.prepareStatement("SELECT COUNT(*) FROM roles WHERE role_code IN ('CUSTOMER','ADMIN')")) {
                    try(var rs=ps.executeQuery()) {
                        rs.next();
                        if(rs.getInt(1)!=2) throw new IllegalStateException("Phase 4 roles are missing");
                    }
                }
                try(var ps=c.prepareStatement("SELECT COUNT(*) FROM users")) {
                    try(var rs=ps.executeQuery()) {rs.next();System.out.println("Identity schema READY; accounts="+rs.getLong(1));}
                }
            }
        } else if(args.length==3 && args[0].equals("grant-admin")
                && args[2].equals("--confirm-local-admin-bootstrap")) {
            String email=IdentityRules.email(args[1]);
            try(Connection c=DbPool.getConnection()) {
                boolean inserted=new AuthDao().grantAdmin(c,email);
                if(!inserted) {
                    var current=new AuthDao().byEmail(c,email);
                    if(current==null || !current.active()) throw new IllegalArgumentException("No active registered account matches that email");
                }
                System.out.println("ADMIN role assigned or already present; sign out and sign in again: "+email);
            }
        } else {
            System.err.println("Usage: IdentityCli migrate | status | grant-admin <registered-email> --confirm-local-admin-bootstrap");
            System.exit(2);
        }
    }
}
