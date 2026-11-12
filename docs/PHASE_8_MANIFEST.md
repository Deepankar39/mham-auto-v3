# Phase 8 — Overlay file manifest

Baseline: Phase 1.3 and Phases 1.4–7 overlays merged in release order. No full-project replacement.

New files: 23

- `database/10_admin_schema.sql`
- `database/11_phase8_validation.sql`
- `docs/PHASE_8_GUIDE.md`
- `docs/PHASE_8_MANIFEST.md`
- `scripts/Test-Phase8-Offline.ps1`
- `scripts/Test-Phase8.ps1`
- `src/main/java/com/mhamauto/admin/AdminDao.java`
- `src/main/java/com/mhamauto/admin/AdminModels.java`
- `src/main/java/com/mhamauto/admin/AdminRules.java`
- `src/main/java/com/mhamauto/cli/AdminCli.java`
- `src/main/java/com/mhamauto/db/AdminMigration.java`
- `src/main/java/com/mhamauto/servlet/AdminServlet.java`
- `src/main/webapp/WEB-INF/views/admin-categories.jsp`
- `src/main/webapp/WEB-INF/views/admin-dashboard.jsp`
- `src/main/webapp/WEB-INF/views/admin-order.jsp`
- `src/main/webapp/WEB-INF/views/admin-orders.jsp`
- `src/main/webapp/WEB-INF/views/admin-product-form.jsp`
- `src/main/webapp/WEB-INF/views/admin-products.jsp`
- `src/main/webapp/WEB-INF/views/fragments/admin-head.jspf`
- `src/main/webapp/WEB-INF/views/fragments/admin-nav.jspf`
- `src/main/webapp/assets/css/admin.css`
- `src/test/java/com/mhamauto/admin/AdminRulesTest.java`
- `tests/Phase8OfflineCheck.java`

Updated existing files: 4

- `src/main/java/com/mhamauto/servlet/AccountServlet.java`
- `src/main/webapp/WEB-INF/views/cart.jsp`
- `src/main/webapp/WEB-INF/views/product-detail.jsp`
- `src/main/webapp/WEB-INF/views/products.jsp`

No earlier database migrations, importers or checkout/order Java source have been replaced. All updated JSP changes outside admin are text-only demo disclosures.
