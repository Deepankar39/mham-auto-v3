# MHAM AUTO | Phase 11 QA, risk and scalability plan

**Release candidate RC1** | 28 October 2026

## Regression strategy

The release uses a layered test pyramid:

1. **Plain-Java rules** — run the existing Phase 1.3–10 rule test classes via `scripts/Test-Phase11-Offline.ps1`. These check validation, formatting, origin rules and pure service logic without a database.
2. **Maven/JUnit/WAR build** — `mvn clean test package` compiles all application and JUnit code, assembles the WAR and exercises JUnit tests.
3. **SQL and importer integration** — inspect `schema_migrations`, validate 26 app tables, import and repeat-import the CSV using an isolated empty MySQL database. Check unique handles, SKUs, media link counts and no unexpected duplicates.
4. **Anonymous HTTP smoke** — `scripts/Test-Phase11-Live.ps1` checks public routes, required security headers, protected-route redirects, and a cross-origin POST denial.
5. **Authenticated journeys** — two CUSTOMER accounts and one ADMIN account complete the cases in `docs/PHASE_11_TEST_MATRIX.csv`. Capture actual results and reproduce failures, rather than inferring from UI presence.
6. **Browser, keyboard and responsive QA** — mobile/desktop layout, safe escaped text, visible focus, fallback images, demo disclosures, price pending states and error displays.

## Entry and exit criteria

- Start from a clean, backed-up test database and a known build.
- No intentional writes to a real user database in performance/UAT runs.
- No skipped mandatory tests may be marked PASS.
- Every issue has test ID, severity, steps, actual/expected result, and a retest outcome.
- Release sign-off requires zero known Critical/High functional or security failures and completed evidence collection.

## 500 → 50k → 1M+ growth plan

| Dataset | Reason | Verification requirement |
|---|---|---|
| Four supplied rows | Exact source structure and import fidelity | Preserve 74 Shopify fields, variants, image links, unknown currency |
| 500 synthetic rows | Academic V1 feature smoke | Import/reimport; unique handles and SKUs; responsive browse/detail |
| 50,000 generated records | Future non-production query tuning | Benchmark cold/warm catalogue pagination, SKU lookup and import batch throughput, record EXPLAIN and DB memory |
| 1,000,000+ generated records | Future release-gate capacity study | Defined CPU/RAM/disk, index statistics, P50/P95 query latency, concurrent sessions, backpressure and rollback plan |

These are **plans**, not measured service-level outcomes. The current container cannot run a MySQL/Tomcat load test, and no statement about 1M+ capacity is independently verified. Do not use the 500 synthetic records as performance evidence for 1M items.

## Recommended evidence record

For every large-scale run, record software version, CPU/RAM/storage, data generator seed, table cardinality, DB index sizes, query text, p50/p95/p99 latency, throughput, errors, and reproducible scripts. Use `database/14_phase10_performance_review.sql` as read-only SQL review material and `scripts/Measure-Phase10.ps1 -Runs 5` for small local latency diagnostics; these alone are not a concurrency benchmark.
