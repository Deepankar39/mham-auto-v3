package com.mhamauto.cli;

import com.mhamauto.db.DbPool;
import java.math.BigDecimal;
import java.sql.Connection;

/** Explicit fictional prices ONLY for [SYNTHETIC TEST] rows. Never processes real sample products. */
public final class DemoPriceCli {
    private DemoPriceCli(){ }
    public static void main(String[] args)throws Exception {
        if(args.length!=2 || !"seed-synthetic".equals(args[0]) || !"--confirm-fictional-kwd".equals(args[1])){
            System.err.println("Usage: DemoPriceCli seed-synthetic --confirm-fictional-kwd");
            System.err.println("Assigns KWD 10.000 to eligible unpriced [SYNTHETIC TEST] products ONLY.");System.exit(2);
        }
        try(Connection c=DbPool.getConnection()){
            c.setAutoCommit(false);
            try{
                int audit=0;int updated=0;
                // Single fixed demonstrative price; the Shopify CSV price is never assumed to be KWD.
                try(var ps=c.prepareStatement("INSERT IGNORE INTO demo_price_assignments(variant_id,assigned_price_kwd) " +
                     "SELECT v.id,10.000 FROM product_variants v JOIN products p ON p.id=v.product_id " +
                     "WHERE p.title LIKE '[SYNTHETIC TEST %' AND v.retail_price_kwd IS NULL")){
                    audit=ps.executeUpdate();
                }
                try(var ps=c.prepareStatement("UPDATE product_variants v JOIN demo_price_assignments d ON d.variant_id=v.id " +
                     "SET v.retail_price_kwd=d.assigned_price_kwd WHERE v.retail_price_kwd IS NULL")){
                    updated=ps.executeUpdate();
                }
                c.commit();System.out.printf("Assigned fictional KWD 10.000: new audit=%d, unpriced variants updated=%d%n",audit,updated);
            }catch(Exception e){try{c.rollback();}catch(Exception rollback){e.addSuppressed(rollback);}throw e;}
            finally{c.setAutoCommit(true);}
        }
    }
}
