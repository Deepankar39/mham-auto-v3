import com.mhamauto.service.CatalogPreviewService;

/** Standalone test used by existing scripts/Test-Phase1_3_Offline.ps1. */
public final class CatalogPreviewServiceCheck {
    public static void main(String[] args) {
        CatalogPreviewService service = new CatalogPreviewService();
        require(service.getCategories().size() == 6, "Expected six preview categories");
        require("Engine Components".equals(service.getCategories().get(0).getName()), "Original category preserved");
        require(service.parseAudience(null) == CatalogPreviewService.Audience.RETAIL, "Default retail selection");
        require(service.parseAudience("ReTaIl") == CatalogPreviewService.Audience.RETAIL, "Case insensitive retail");
        require(service.parseAudience("trade") == CatalogPreviewService.Audience.TRADE, "Trade selection");
        boolean rejected = false;
        try { service.parseAudience("enterprise"); } catch (IllegalArgumentException expected) { rejected = true; }
        require(rejected, "Reject unsupported audience");
        System.out.println("PASS: Phase 1.3 offline service regression");
    }
    private static void require(boolean valid, String reason) {
        if (!valid) throw new AssertionError(reason);
    }
}
