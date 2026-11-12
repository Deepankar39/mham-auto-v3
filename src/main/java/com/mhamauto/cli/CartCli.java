package com.mhamauto.cli;

import com.mhamauto.db.CartMigration;
import com.mhamauto.db.DbPool;
import java.nio.file.Path;
import java.sql.Connection;

/** Administrative local-only CLI. No public migration endpoint. */
public final class CartCli {
    private CartCli() { }
    public static void main(String[] args) throws Exception {
        if(args.length!=1 || !("migrate".equals(args[0]) || "status".equals(args[0]) || "sample-variant".equals(args[0]))) {
            System.err.println("Usage: CartCli migrate | status | sample-variant"); System.exit(2);
        }
        try(Connection c=DbPool.getConnection()) {
            if("migrate".equals(args[0])) System.out.println(CartMigration.apply(c,Path.of("database","05_cart_schema.sql")));
            else if("sample-variant".equals(args[0])) {
                try(var p=c.prepareStatement("SELECT x.id FROM product_variants x JOIN products pr ON pr.id=x.product_id " +
                  "LEFT JOIN inventory i ON i.variant_id=x.id LEFT JOIN variant_market_availability ma " +
                  "ON ma.variant_id=x.id AND ma.market_key='KW' WHERE pr.status='active' AND pr.published=TRUE " +
                  "AND (ma.included IS NULL OR ma.included=TRUE) AND (i.source_quantity IS NULL OR i.source_quantity>=3) " +
                  "ORDER BY x.id LIMIT 1")) {
                    try(var r=p.executeQuery()) {if(!r.next()) throw new IllegalStateException("No suitable visible sample variant found");System.out.println(r.getLong(1));}
                }
            }
            else {
                try(var p=c.prepareStatement("SELECT COUNT(*) FROM schema_migrations WHERE version='V003'")) {
                    try(var r=p.executeQuery()) {r.next(); if(r.getInt(1)!=1) throw new IllegalStateException("V003 has not been applied");}
                }
                try(var p=c.prepareStatement("SELECT (SELECT COUNT(*) FROM carts),(SELECT COUNT(*) FROM cart_items)")) {
                    try(var r=p.executeQuery()) {r.next();System.out.printf("Cart schema READY; customer carts=%d; lines=%d%n",r.getLong(1),r.getLong(2));}
                }
            }
        }
    }
}
