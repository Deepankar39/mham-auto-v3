import com.mhamauto.config.AppSettings;
import com.mhamauto.media.ExternalImageUrlPolicy;
import com.mhamauto.web.ViewPaths;

/** Standalone Java 21 checks: no MySQL, JUnit or Tomcat required. */
public final class AppFoundationCheck {
    public static void main(String[] args) {
        AppSettings settings = AppSettings.load();
        require("KW".equals(settings.market()), "Kuwait market");
        require("KWD".equals(settings.currency()), "KWD currency");
        require(settings.demoMode(), "No live transactions");
        require(ViewPaths.CATALOG_PREVIEW.startsWith("/WEB-INF/"), "Protected JSP view");

        require(ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com/s/files/1/0000/test.jpg?v=123"), "Shopify HTTPS URL");
        require(ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com/shop/files/item.png"), "Shopify alternate CDN path");
        require(!ExternalImageUrlPolicy.isAllowed("http://cdn.shopify.com/s/files/test.jpg"), "Reject HTTP");
        require(!ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com.evil.example/photo.jpg"), "Reject spoofed host");
        require(!ExternalImageUrlPolicy.isAllowed("https://evil.example/cdn.shopify.com/image.jpg"), "Reject unsupported host");
        require(!ExternalImageUrlPolicy.isAllowed("https://user@cdn.shopify.com/x.jpg"), "Reject user info");
        require(!ExternalImageUrlPolicy.isAllowed("file:///etc/passwd"), "Reject file URI");
        require(!ExternalImageUrlPolicy.isAllowed("javascript:alert(1)"), "Reject executable URL");
        require(!ExternalImageUrlPolicy.isAllowed("https://cdn.shopify.com/x.jpg#frag"), "Reject fragments");
        require(!ExternalImageUrlPolicy.isAllowed(" https://cdn.shopify.com/x.jpg"), "Reject untrimmed input");
        require(!ExternalImageUrlPolicy.isAllowed(null), "Reject missing URL");
        System.out.println("PASS: Phase 1.4 settings, protected paths and external image URL policy");
    }
    private static void require(boolean valid, String reason) {
        if (!valid) throw new AssertionError(reason);
    }
}
