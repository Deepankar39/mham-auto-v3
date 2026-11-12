# Phase 9 — changed-file manifest

This package is an **incremental overlay** for the existing MHAM AUTO source. It is not a standalone Maven project.

- New files: 19
- Modified existing files: 5
- Removed files: **none**
- Old migrations modified: **none**
- Older product/cart/order/identity Java classes modified: **none**

## New files
- `database/12_trade_application_schema.sql`
- `database/13_phase9_validation.sql`
- `docs/PHASE_9_GUIDE.md`
- `scripts/Test-Phase9-Offline.ps1`
- `scripts/Test-Phase9.ps1`
- `src/main/java/com/mhamauto/cli/TradeCli.java`
- `src/main/java/com/mhamauto/db/TradeMigration.java`
- `src/main/java/com/mhamauto/servlet/AdminTradeServlet.java`
- `src/main/java/com/mhamauto/servlet/TradeApplicationServlet.java`
- `src/main/java/com/mhamauto/trade/TradeDao.java`
- `src/main/java/com/mhamauto/trade/TradeModels.java`
- `src/main/java/com/mhamauto/trade/TradeRules.java`
- `src/main/webapp/WEB-INF/views/admin-trade-application.jsp`
- `src/main/webapp/WEB-INF/views/admin-trade-applications.jsp`
- `src/main/webapp/WEB-INF/views/trade-apply.jsp`
- `src/main/webapp/WEB-INF/views/trade-status.jsp`
- `src/main/webapp/assets/css/trade.css`
- `src/test/java/com/mhamauto/trade/TradeRulesTest.java`
- `tests/Phase9OfflineCheck.java`

## Modified files
- `src/main/java/com/mhamauto/filter/ProtectedPagesFilter.java`
- `src/main/webapp/WEB-INF/views/account.jsp`
- `src/main/webapp/WEB-INF/views/admin-dashboard.jsp`
- `src/main/webapp/WEB-INF/views/fragments/admin-nav.jspf`
- `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf`

## Important migration boundary

`database/12_trade_application_schema.sql` is **V006**, appended after Phase 8 V005. It only creates `business_applications` and `business_application_events`. It does not update existing user roles, prices, products, cart rows, customer addresses or demonstration orders.

## Verification performed here

- Phase 9 pure-Java validation smoke checks via `javac` and `java`: PASS.
- New Phase 9 Java source compilation against lightweight local servlet and DB-pool test signatures: PASS.
- ZIP structure, syntax-oriented file checks and overlay diff validation: PASS.
- NOT yet executed here: full Maven/JUnit and JSP compilation, live MySQL V006 migration, Tomcat authentication paths, or multi-account browser testing. Run the provided Windows tests and manual checklist.
