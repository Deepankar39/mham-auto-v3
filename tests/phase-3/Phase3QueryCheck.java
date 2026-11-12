import com.mhamauto.catalog.CatalogModels;
import com.mhamauto.catalog.CatalogQuery;
import com.mhamauto.catalog.DescriptionText;
import com.mhamauto.media.ExternalImageUrlPolicy;
import java.math.BigDecimal;

public class Phase3QueryCheck {
    private static void valid(boolean v,String why){if(!v)throw new AssertionError(why);}
    private static void invalid(Runnable code,String why) {
        try{code.run();throw new AssertionError("Expected rejection: "+why);}catch(IllegalArgumentException expected){}
    }
    public static void main(String[] unused){
        var q=CatalogQuery.parse("  wheel hub ","12","uncategorized","title_asc","2");
        valid(q.query().equals("wheel hub") && q.offset()==24 && q.uncategorized(),"Parsing & paging");
        valid(q.fullTextTerms().equals("+wheel* +hub*"),"Fulltext tokenization");
        valid(q.href(3).contains("wheel+hub") && q.href(3).contains("category=uncategorized"),"Encoded paging URL");
        valid(CatalogQuery.parse("V_12%!",null,null,null,null).skuPrefix().equals("V!_12!%!!%"),"Literal prefix escaping");
        invalid(()->CatalogQuery.parse("x",null,null,"DROP TABLE products",null),"SQL sort allowlist");
        invalid(()->CatalogQuery.parse("x","-1",null,null,null),"Negative vendor id");
        invalid(()->CatalogQuery.parse("x",null,null,null,"10001"),"Deep page bound");
        invalid(()->CatalogQuery.parse("x".repeat(81),null,null,null,null),"Query bound");
        invalid(()->CatalogQuery.parse("hello\nworld",null,null,null,null),"Control character");
        valid(DescriptionText.excerpt("<script>alert(1)</script><p>Safe &amp; sound</p>").equals("Safe & sound"),"Markup stripped");
        var card=new CatalogModels.Card(3,"a","Title","Vendor","Uncategorised","SKU",null,"http://bad.test/a.jpg");
        valid(card.getPriceLabel().equals("Price pending verification"),"Unverified price protected");
        valid(card.getImageUrl().isEmpty(),"Blocked image URL");
        valid(ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com/s/files/1/product.jpg"),"Valid Shopify CDN");
        valid(!ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com.evil.org/a.jpg"),"Forged CDN host rejected");
        valid(new CatalogModels.Card(5,"foo","Title","Vendor","Category","SKU",new BigDecimal("21.123"),null)
            .getPriceLabel().equals("KWD 21.123"),"3 decimal price");
        System.out.println("PASS Phase 3 Java query, URL, price, media and description checks");
    }
}
