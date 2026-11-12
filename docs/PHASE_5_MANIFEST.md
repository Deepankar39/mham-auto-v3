# MHAM AUTO Phase 5 — Incremental overlay manifest

- Phase 5 is additive over the user's working Phase 4 installation, not a reset.
- No changes to `pom.xml`, existing V001/V002 scripts, Phase 2 importer, Phase 3 search SQL, homepage JSP or previously imported catalogue data.
- Existing files **updated**:
  - `src/main/java/com/mhamauto/catalog/CatalogModels.java` — JavaBean getter for variant ID in JSP.
  - `src/main/java/com/mhamauto/filter/ProtectedPagesFilter.java` — authenticate and DB-refresh on `/cart`.
  - `src/main/java/com/mhamauto/servlet/ProductDetailServlet.java` — issue cart CSRF for authenticated visitors.
  - `src/main/webapp/WEB-INF/views/product-detail.jsp` — variant add-to-cart form.
  - `src/main/webapp/WEB-INF/views/account.jsp` — account dashboard cart link and phase copy.
  - `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf` — cart link and dynamic badge.
  - `src/main/webapp/WEB-INF/views/fragments/home-header.jspf` — cart link and dynamic badge.
- **New files**:
  - `database/05_cart_schema.sql`
  - `database/06_phase5_validation.sql`
  - `src/main/java/com/mhamauto/db/CartMigration.java`
  - `src/main/java/com/mhamauto/cli/CartCli.java`
  - `src/main/java/com/mhamauto/cart/CartRules.java`
  - `src/main/java/com/mhamauto/cart/CartModels.java`
  - `src/main/java/com/mhamauto/cart/CartConflict.java`
  - `src/main/java/com/mhamauto/cart/CartDao.java`
  - `src/main/java/com/mhamauto/cart/CartService.java`
  - `src/main/java/com/mhamauto/servlet/CartServlet.java`
  - `src/main/java/com/mhamauto/servlet/CartCountServlet.java`
  - `src/main/webapp/WEB-INF/views/cart.jsp`
  - `src/main/webapp/assets/css/cart.css`
  - `src/main/webapp/assets/js/cart.js`
  - `src/main/webapp/assets/js/cart-badge.js`
  - `src/test/java/com/mhamauto/cart/CartRulesTest.java`
  - `tests/phase-5/Phase5CartCheck.java`
  - `scripts/Test-Phase5-Offline.ps1`
  - `scripts/Test-Phase5.ps1`
  - `docs/PHASE_5_GUIDE.md`
  - `docs/PHASE_5_ARCHITECTURE.md`
  - `docs/PHASE_5_MANIFEST.md`

No image binaries are embedded in MySQL; product images remain Shopify CDN URLs from Phase 2. The MySQL V003 migration is additive only, with no DROP or TRUNCATE statements.

**Verification evidence in packaging environment:** Java 21 cart/source/test compile using temporary Jakarta Servlet API and JUnit signatures, standalone offline cart tests, Node JS syntax checks, archive integrity. **Not executed here:** live Maven build, JSP/Tomcat compilation, MySQL DDL, browser/end-to-end regression. Those must be checked locally before marking Phase 5 complete.
