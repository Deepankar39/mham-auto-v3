package com.mhamauto.cli;

import com.mhamauto.catalog.CatalogDao;
import com.mhamauto.catalog.CatalogQuery;
import com.mhamauto.db.DbConfig;
import java.sql.Connection;
import java.sql.DriverManager;

/** Non-destructive Phase 3 smoke: checks actual MySQL catalogue queries from the command line. */
public final class CatalogReadCli {
    private CatalogReadCli() { }
    public static void main(String[] args) throws Exception {
        DbConfig cfg=DbConfig.fromEnvironment();
        try(Connection c=DriverManager.getConnection(cfg.url(),cfg.username(),cfg.password())) {
            CatalogDao dao=new CatalogDao();
            var first=dao.search(c,CatalogQuery.parse(null,null,null,null,null));
            long minimum=args.length==0?4:Long.parseLong(args[0]);
            if (first.total()<minimum || first.cards().isEmpty()) {
                throw new IllegalStateException("Expected at least " + minimum + " visible Kuwait products, found " + first.total());
            }
            var example=first.cards().getFirst();
            var detail=dao.detail(c,example.handle());
            if(detail==null || detail.variants().isEmpty()) throw new IllegalStateException("Missing product detail/variants");
            var bySku=dao.search(c,CatalogQuery.parse(detail.variants().getFirst().sku(),null,null,null,null));
            if (bySku.total()<1) throw new IllegalStateException("SKU search failed");
            var byVendor=dao.search(c,CatalogQuery.parse(null,null,null,"title_asc",null));
            if(byVendor.total()!=first.total()) throw new IllegalStateException("Sort changed count");
            if(dao.detail(c,"nonexistent-mham-phase3-product") != null) throw new IllegalStateException("Missing product not 404");
            System.out.println("PASS Phase 3 MySQL read-only checks: total=" + first.total() + " SKU search / details / sorting");
            System.out.println("PRODUCT_HANDLE=" + example.handle());
        }
    }
}
