import com.mhamauto.db.DbConfig;
import com.mhamauto.importer.ShopifyRecord;
import com.mhamauto.media.ExternalImageUrlPolicy;
import java.util.Map;

public class Phase2PlainJavaCheck {
    public static void main(String[] ignored) {
        ShopifyRecord record=new ShopifyRecord(2,Map.of("Handle","part-a","Variant SKU","'8113206",
                "Variant Price","69","Image Src","https://cdn.shopify.com/pic.jpg"));
        record.validate("UNKNOWN");
        if (!"'8113206".equals(record.sku()) || record.kwdPrice("UNKNOWN")!=null) throw new AssertionError("Unverified data corrupted");
        if (!"69".equals(record.kwdPrice("KWD").toPlainString())) throw new AssertionError("KWD precision wrong");
        if (ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com.evil.test/pic.jpg")) throw new AssertionError("Unsafe CDN host");
        try { DbConfig.fromMap(Map.of()); throw new AssertionError("DB secrets unexpectedly optional"); }
        catch(IllegalStateException expected) { /* correct */ }
        System.out.println("PASS Phase 2: SKU preservation, unknown currency, CDN whitelist, environment secrets");
    }
}
