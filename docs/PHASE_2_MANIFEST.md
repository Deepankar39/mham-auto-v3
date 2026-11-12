# MHAM AUTO Phase 2 Overlay Manifest

- Base: Phase 1.3 user source + completed Phase 1.4 overlay.
- Modified existing file: `pom.xml` only (runtime dependencies + JUnit runner).
- New Java: `db/*`, `dao/CatalogImportDao.java`, `importer/*`, `cli/CatalogCli.java`, `servlet/DatabaseStatusServlet.java`.
- New schema: `database/00_create_database.sql`, `database/01_catalog_schema.sql`, read-only `database/02_phase2_validation.sql`.
- Tests: `src/test/java/com/mhamauto/importer/*`, `tests/phase-2/*`, scripts.
- Samples: `samples/product data sample.csv` (original 74-column/four-row input) and `samples/SYNTHETIC_TEST_500.csv` (distinct synthetic handles/SKUs, repeated original 4 CDN image URLs; test-only, not legitimate inventory).
- Documentation: PHASE_2_GUIDE, PHASE_2_MANIFEST, SHOPIFY_CSV_74_COLUMN_MATRIX, PHASE_2_DATABASE_ERD, PHASE_2_TEST_RESULTS.
- No source deletion. **Do not overwrite `src/main/webapp/index.jsp`, existing preview JSP or its assets**, none are in the Phase 2 overlay.
- Existing `/health` is liveness only; `/api/db-status` provides independent DB readiness checks.
- Files under `target/` are generated locally by Maven; never extracted from ZIP.
