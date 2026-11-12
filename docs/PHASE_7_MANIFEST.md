# Phase 7 incremental overlay manifest

## New
- `src/main/java/com/mhamauto/account/PortalRules.java` — validated page/reference/profile rules.
- `src/main/java/com/mhamauto/account/PortalModels.java` — JSP-friendly summaries and order cards.
- `src/main/java/com/mhamauto/account/PortalDao.java` — owner-scoped dashboard/history/profile SQL.
- `src/main/java/com/mhamauto/servlet/CustomerOrdersServlet.java` — history and detail endpoints.
- `src/main/webapp/WEB-INF/views/orders.jsp` — history with pagination and empty state.
- `src/main/webapp/WEB-INF/views/order-detail.jsp` — read-only snapshot details.
- `src/main/webapp/assets/css/customer-portal.css` — isolated responsive portal styling.
- `src/test/java/com/mhamauto/account/PortalRulesTest.java` — JUnit 5 validation checks.
- `tests/phase-7/Phase7PortalCheck.java` — plain-Java offline test.
- `scripts/Test-Phase7-Offline.ps1` and `scripts/Test-Phase7.ps1` — verification.
- `database/09_phase7_validation.sql` — optional read-only checks, no migration.
- `docs/PHASE_7_GUIDE.md` and this manifest.

## Updated existing files
- `src/main/java/com/mhamauto/servlet/AccountServlet.java` — account dashboard and CSRF-protected name change; `/admin` behavior retained.
- `src/main/java/com/mhamauto/filter/ProtectedPagesFilter.java` — protect `/orders` and `/orders/*` using existing fresh-role checks.
- `src/main/webapp/WEB-INF/views/account.jsp` — dashboard/profile/history quick access.
- `src/main/webapp/WEB-INF/views/order-confirmation.jsp` — Phase 7 history navigation text.
- `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf` — My orders navigation link.

## Unchanged
- `pom.xml`; all Phase 1–6 SQL schemas, importers, products, cart, address and checkout services, payment exclusion, historical snapshots, product images and assets.

## Requirements
- Java 21, Maven 3.9.11, Tomcat 10.1.61, MySQL schema through Phase 6.
- Verify Phase 6 success before rolling out Phase 7.
