# Phase 6 — incremental changed-file manifest

This is an **overlay**, not a full standalone project. Extract it into `D:\Java Project\MHAM AUTO` after backing up the existing project and database.

## Modified existing files (3)
- `src/main/java/com/mhamauto/filter/ProtectedPagesFilter.java`
- `src/main/webapp/WEB-INF/views/account.jsp`
- `src/main/webapp/WEB-INF/views/cart.jsp`

## New files (28)
- `database/07_checkout_schema.sql`
- `database/08_phase6_validation.sql`
- `docs/PHASE_6_ARCHITECTURE.md`
- `docs/PHASE_6_GUIDE.md`
- `docs/PHASE_6_MANIFEST.md`
- `scripts/Test-Phase6-Offline.ps1`
- `scripts/Test-Phase6.ps1`
- `src/main/java/com/mhamauto/checkout/AddressDao.java`
- `src/main/java/com/mhamauto/checkout/AddressRules.java`
- `src/main/java/com/mhamauto/checkout/AddressService.java`
- `src/main/java/com/mhamauto/checkout/CheckoutConflict.java`
- `src/main/java/com/mhamauto/checkout/CheckoutDao.java`
- `src/main/java/com/mhamauto/checkout/CheckoutModels.java`
- `src/main/java/com/mhamauto/checkout/CheckoutRules.java`
- `src/main/java/com/mhamauto/checkout/CheckoutService.java`
- `src/main/java/com/mhamauto/checkout/CheckoutToken.java`
- `src/main/java/com/mhamauto/cli/CheckoutCli.java`
- `src/main/java/com/mhamauto/cli/DemoPriceCli.java`
- `src/main/java/com/mhamauto/db/CheckoutMigration.java`
- `src/main/java/com/mhamauto/servlet/AddressServlet.java`
- `src/main/java/com/mhamauto/servlet/CheckoutServlet.java`
- `src/main/webapp/WEB-INF/views/addresses.jsp`
- `src/main/webapp/WEB-INF/views/checkout.jsp`
- `src/main/webapp/WEB-INF/views/order-confirmation.jsp`
- `src/main/webapp/assets/css/checkout.css`
- `src/test/java/com/mhamauto/checkout/AddressRulesTest.java`
- `src/test/java/com/mhamauto/checkout/CheckoutRulesTest.java`
- `tests/phase-6/Phase6CheckoutCheck.java`

Existing `pom.xml`, homepage, catalogue services, cart schema V003 and Shopify importer are not overwritten. V004 is additive and has a checksum.
