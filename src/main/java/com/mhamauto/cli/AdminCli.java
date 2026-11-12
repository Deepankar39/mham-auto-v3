package com.mhamauto.cli;

import com.mhamauto.db.AdminMigration;
import com.mhamauto.db.DbPool;
import java.nio.file.Path;

public final class AdminCli {
    private AdminCli() { }
    public static void main(String[] args) throws Exception {
        if(args.length!=1 || !("migrate".equals(args[0])||"status".equals(args[0]))) {
            System.err.println("Usage: AdminCli migrate | status");System.exit(2);
        }
        try(var c=DbPool.getConnection()) {
            if("migrate".equals(args[0]))System.out.println(AdminMigration.apply(c,Path.of("database","10_admin_schema.sql")));
            else {
                try(var p=c.prepareStatement("SELECT COUNT(*) FROM schema_migrations WHERE version='V005'")){
                    try(var r=p.executeQuery()){r.next();if(r.getLong(1)!=1)throw new IllegalStateException("V005 missing");}
                }
                try(var p=c.prepareStatement("SELECT (SELECT COUNT(*) FROM admin_events),(SELECT COUNT(*) FROM products),(SELECT COUNT(*) FROM demo_orders)")){
                    try(var r=p.executeQuery()){r.next();System.out.printf("V005 READY: audit=%d products=%d demo orders=%d%n",r.getLong(1),r.getLong(2),r.getLong(3));}
                }
            }
        }
    }
}
