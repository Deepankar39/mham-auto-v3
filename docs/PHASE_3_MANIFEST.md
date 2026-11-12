# Phase 3 overlay manifest

Only listed paths are included in the overlay. Restore the project's pre-Phase3 backup to roll back source files if necessary. The database is not changed.

## Existing files replaced
- `pom.xml` (adds Jakarta JSTL 3 API + implementation)
- `src/main/webapp/index.jsp` (new database catalogue navigation link)
- `src/main/webapp/WEB-INF/views/catalog-preview.jsp` (adds link to database-backed catalogue; original preview remains)
- `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf` (link to live catalogue; preview unchanged)
- `src/main/webapp/WEB-INF/views/fragments/catalog-footer.jspf` (release label)

## Added files
- `src/main/java/com/mhamauto/catalog/CatalogQuery.java`
- `src/main/java/com/mhamauto/catalog/CatalogModels.java`
- `src/main/java/com/mhamauto/catalog/CatalogDao.java`
- `src/main/java/com/mhamauto/catalog/DescriptionText.java`
- `src/main/java/com/mhamauto/servlet/ProductBrowseServlet.java`
- `src/main/java/com/mhamauto/servlet/ProductDetailServlet.java`
- `src/main/java/com/mhamauto/cli/CatalogReadCli.java`
- `src/main/webapp/WEB-INF/views/products.jsp`
- `src/main/webapp/WEB-INF/views/product-detail.jsp`
- `src/main/webapp/assets/css/products.css`
- `src/main/webapp/assets/js/products.js`
- `src/test/java/com/mhamauto/catalog/CatalogQueryTest.java`
- `tests/phase-3/Phase3QueryCheck.java`
- `scripts/Test-Phase3-Offline.ps1`
- `scripts/Test-Phase3.ps1`
- `docs/PHASE_3_GUIDE.md`
- `docs/PHASE_3_MANIFEST.md`

## Never included
No `.env` files, database credentials, media binaries, `target/`, Tomcat install, real order/payment features, or schema destructive scripts.
