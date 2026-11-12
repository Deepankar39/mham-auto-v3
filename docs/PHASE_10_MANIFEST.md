# MHAM AUTO Academic V1 — Phase 10 change manifest

Incremental overlay after approved Phase 9. No code or data from Phases 1–9 is removed.
No MySQL migration. Source and application modifications are additive or localized.

## New files (11)

- `database/14_phase10_performance_review.sql`
- `docs/PHASE_10_GUIDE.md`
- `scripts/Measure-Phase10.ps1`
- `scripts/Test-Phase10-Offline.ps1`
- `scripts/Test-Phase10.ps1`
- `src/main/java/com/mhamauto/web/RequestSecurity.java`
- `src/main/webapp/assets/css/phase10.css`
- `src/main/webapp/assets/js/phase10.js`
- `src/test/java/com/mhamauto/web/RequestDefaultsFilterTest.java`
- `src/test/java/com/mhamauto/web/RequestSecurityTest.java`
- `tests/Phase10OfflineCheck.java`

## Updated files (10)

- `src/main/java/com/mhamauto/catalog/CatalogDao.java`
- `src/main/java/com/mhamauto/filter/RequestDefaultsFilter.java`
- `src/main/webapp/WEB-INF/views/admin-products.jsp`
- `src/main/webapp/WEB-INF/views/catalog-preview.jsp`
- `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf`
- `src/main/webapp/WEB-INF/views/fragments/document-basics.jspf`
- `src/main/webapp/WEB-INF/views/fragments/home-header.jspf`
- `src/main/webapp/assets/js/cart-badge.js`
- `src/main/webapp/assets/js/catalog-preview.js`
- `src/main/webapp/index.jsp`

## Local validation evidence

- Java 21 plain-Java origin/CSP checks: PASS
- Java DAO syntax check: PASS
- RequestDefaultsFilter servlet API test-signature compilation: PASS
- JUnit test-source syntax check using isolated test signatures: PASS
- JavaScript syntax checks (`node --check`): PASS
- Static CSP inline-attribute scan over JSPs: PASS
- Maven dependency resolution, Tomcat JSP rendering, MySQL and browser flows: NOT RUN here; local verification required

The only existing Java files updated are the shared HTTP filter and catalogue DAO.
