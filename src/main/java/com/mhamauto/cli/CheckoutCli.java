package com.mhamauto.cli;

import com.mhamauto.db.CheckoutMigration;
import com.mhamauto.db.DbPool;
import java.nio.file.Path;
import java.sql.Connection;

/** V004 local-only migration and read-only schema status. */
public final class CheckoutCli {
    private CheckoutCli(){ }
    public static void main(String[] args)throws Exception {
        if(args.length!=1 || !(args[0].equals("migrate")||args[0].equals("status")||args[0].equals("sample-priced"))){
            System.err.println("Usage: CheckoutCli migrate | status | sample-priced");System.exit(2);
        }
        try(Connection c=DbPool.getConnection()){
            if("migrate".equals(args[0]))System.out.println(CheckoutMigration.apply(c,Path.of("database","07_checkout_schema.sql")));
            else if("sample-priced".equals(args[0])){
                try(var ps=c.prepareStatement("SELECT v.id FROM product_variants v JOIN products p ON p.id=v.product_id " +
                     "LEFT JOIN inventory i ON i.variant_id=v.id " +
                     "LEFT JOIN variant_market_availability ma ON ma.variant_id=v.id AND ma.market_key='KW' " +
                     "WHERE p.status='active' AND p.published=TRUE AND (ma.included IS NULL OR ma.included=TRUE) " +
                     "AND (i.source_quantity IS NULL OR i.source_quantity>=2) AND v.retail_price_kwd IS NOT NULL " +
                     "ORDER BY v.id LIMIT 1")){
                    try(var rs=ps.executeQuery()){if(!rs.next())throw new IllegalStateException("No KWD-priced eligible variant found. Assign fictional demo prices only to SYNTHETIC rows if desired");System.out.println(rs.getLong(1));}
                }
            } else {
                try(var ps=c.prepareStatement("SELECT COUNT(*) FROM schema_migrations WHERE version='V004'")){
                    try(var rs=ps.executeQuery()){rs.next();if(rs.getInt(1)!=1)throw new IllegalStateException("V004 migration missing");}
                }
                try(var ps=c.prepareStatement("SELECT (SELECT COUNT(*) FROM customer_addresses),(SELECT COUNT(*) FROM demo_orders),(SELECT COUNT(*) FROM demo_order_items)")){
                    try(var rs=ps.executeQuery()){rs.next();System.out.printf("V004 READY: addresses=%d, demo orders=%d, snapshot lines=%d%n",rs.getLong(1),rs.getLong(2),rs.getLong(3));}
                }
            }
        }
    }
}
