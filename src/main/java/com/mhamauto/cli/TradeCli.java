package com.mhamauto.cli;

import com.mhamauto.db.DbPool;
import com.mhamauto.db.TradeMigration;
import java.nio.file.Path;

/** Operator-only migration and read-only counts. Does not grant privileges. */
public final class TradeCli {
    private TradeCli() { }
    public static void main(String[] args) throws Exception {
        if(args.length!=1 || !("migrate".equals(args[0])||"status".equals(args[0]))) {
            System.err.println("Usage: TradeCli migrate | status");System.exit(2);
        }
        try(var c=DbPool.getConnection()) {
            if("migrate".equals(args[0])) {
                System.out.println(TradeMigration.apply(c,Path.of("database","12_trade_application_schema.sql")));
                return;
            }
            try(var p=c.prepareStatement("SELECT 1 FROM schema_migrations WHERE version='V006'")){
                try(var r=p.executeQuery()){if(!r.next())throw new IllegalStateException("V006 missing");}
            }
            try(var p=c.prepareStatement("SELECT (SELECT COUNT(*) FROM business_applications),(SELECT COUNT(*) FROM business_application_events)")){
                try(var r=p.executeQuery()){r.next();System.out.printf("V006 READY: applications=%d history_events=%d%n",r.getLong(1),r.getLong(2));}
            }
        }
    }
}
