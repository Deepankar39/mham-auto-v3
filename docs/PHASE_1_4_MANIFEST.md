# MHAM AUTO Academic V1 — Phase 1.4 Overlay Manifest

Only paths listed here will be added or replaced. Extract over the EXISTING Phase 1.3 Maven project after backing up. The ZIP contains **no** `pom.xml`, `.env`, existing CSS/JS/graphics, target build output, Tomcat files, or database credentials.

## Replaced source files

- `src/main/java/com/mhamauto/servlet/CatalogPreviewServlet.java`
- `src/main/webapp/WEB-INF/views/catalog-preview.jsp`
- `src/main/webapp/index.jsp`

## New files

- `docs/ARCHITECTURE_V1.md`
- `docs/PHASE_1_4_GUIDE.md`
- `docs/PRODUCT_CSV_MAPPING_CONTRACT.md`
- `scripts/Test-Phase1_4.ps1`
- `scripts/Test-Phase1_4_Offline.ps1`
- `src/main/java/com/mhamauto/config/AppBootstrapListener.java`
- `src/main/java/com/mhamauto/config/AppSettings.java`
- `src/main/java/com/mhamauto/filter/RequestDefaultsFilter.java`
- `src/main/java/com/mhamauto/media/ExternalImageUrlPolicy.java`
- `src/main/java/com/mhamauto/servlet/HealthServlet.java`
- `src/main/java/com/mhamauto/web/ViewPaths.java`
- `src/main/resources/mham-auto.properties`
- `src/main/webapp/WEB-INF/views/error.jsp`
- `src/main/webapp/WEB-INF/views/fragments/catalog-footer.jspf`
- `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf`
- `src/main/webapp/WEB-INF/views/fragments/document-basics.jspf`
- `src/main/webapp/WEB-INF/views/fragments/home-footer.jspf`
- `src/main/webapp/WEB-INF/views/fragments/home-header.jspf`
- `src/main/webapp/WEB-INF/web.xml`
- `src/main/webapp/assets/css/error.css`
- `tests/phase-1.3/CatalogPreviewServiceCheck.java`
- `tests/phase-1.4/AppFoundationCheck.java`
- `docs/PHASE_1_4_MANIFEST.md`

## Applied logic
- JSP includes extract existing header/footer markup without changing the preview category contents. Home receives a third CTA link to `/catalog`.
- The existing preview Servlet changes only its JSP path constant/import.
- No catalogue DB/CSV import is claimed in this phase; see Phase 2 roadmap and `PRODUCT_CSV_MAPPING_CONTRACT.md`.
- Restore baseline at any time from your pre-extraction project backup.

## Validation state
- Local standalone Java 21 tests for preview service, settings and CDN URL policy: PASS.
- All Java source syntax type-checked against temporary minimal Jakarta signatures: PASS (not a real Tomcat test).
- Original CSS, JS and SVG assets byte-for-byte unchanged: PASS.
- Maven WAR/Tomcat live deployment **must be run by project owner** with supplied PowerShell scripts. The packaging environment has no Maven/Tomcat 10.1 runtime.
