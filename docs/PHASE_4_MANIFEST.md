# Phase 4 manifest — additive patch over tested Phase 3

## Existing files modified
- `src/main/webapp/WEB-INF/web.xml`: session cookie/timeout configuration.
- `src/main/webapp/WEB-INF/views/fragments/catalog-header.jspf`: account navigation.
- `src/main/webapp/WEB-INF/views/fragments/home-header.jspf`: account navigation.

## New files
- `database/03_identity_schema.sql`, `database/04_phase4_validation.sql`
- `src/main/java/com/mhamauto/auth/{IdentityRules,PasswordHasher,AuthPrincipal,Csrf,SessionAuth,LoginThrottle,AuthDao,AuthService}.java`
- `src/main/java/com/mhamauto/db/IdentityMigration.java`
- `src/main/java/com/mhamauto/cli/IdentityCli.java`
- `src/main/java/com/mhamauto/filter/ProtectedPagesFilter.java`
- `src/main/java/com/mhamauto/servlet/{IdentityServlet,AccountServlet}.java`
- `src/main/webapp/META-INF/context.xml`
- `src/main/webapp/WEB-INF/views/{login,register,account,admin-entry}.jsp`
- `src/main/webapp/assets/css/auth.css`
- `src/main/webapp/assets/js/auth.js`
- `src/test/java/com/mhamauto/auth/{IdentityRulesTest,PasswordHasherTest,LoginThrottleTest}.java`
- `tests/phase-4/Phase4AuthCheck.java`
- `scripts/Test-Phase4-Offline.ps1`, `scripts/Test-Phase4.ps1`
- `docs/PHASE_4_GUIDE.md`, `docs/PHASE_4_MANIFEST.md`

## Not touched
- Original homepage layout/CSS, catalogue services, imported product/image tables, Phase 2 importer, Phase 3 browse/detail queries, all Phase 1–3 tests and `pom.xml`.
- No database drop or replacement; Phase 4 adds versioned V002 tables.

## Validation boundary
- Plain-Java password/rules/throttle check executed by the package author; Java servlet compilation checked using temporary local Jakarta API type stubs.
- Full Maven/JUnit, MySQL migration and Tomcat integration **must be verified on the user's Windows machine**. No successful live MySQL or deployed Tomcat result is claimed for this package.
