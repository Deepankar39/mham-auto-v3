# MHAM AUTO Academic V1 — Phase 3 Setup & Acceptance

## Scope and dependencies

Read-only database-backed catalogue with filters, indexed full-text name/SKU-prefix search, pages of 24 products, product detail with variants and Shopify CDN gallery. Preserves homepage, six-category Phase 1.3 preview at `/catalog`, and Phase 2 import CLI.

No new database migrations. Phase 2 schema `V001` must have been applied and contain *at least 4 visible Kuwait products*. This overlay adds JSTL 3 dependencies to the existing `pom.xml`.

## Install — Windows PowerShell

1. Back up your existing project. Extract this overlay to `D:\Java Project\MHAM AUTO`, retaining paths and allowing replacement of only the files listed in `PHASE_3_MANIFEST.md`.
2. Open PowerShell in your project folder. Ensure MySQL 8 is running and your Phase 2 `MHAM_DB_USER` and `MHAM_DB_PASSWORD` environment variables are set. If Tomcat was launched from an older window, restart it from the window with these variables so its JVM inherits them. Never commit or share these secrets.
3. Run:

```powershell
Set-Location -LiteralPath 'D:\Java Project\MHAM AUTO'
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase3-Offline.ps1'
mvn clean package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
$cp='target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.CatalogReadCli 4
```

4. Deploy `target\mham-auto.war` to `D:\Java Tools\apache-tomcat-10.1.61\webapps\mham-auto.war`. Restart Tomcat so the updated JSTL libraries and Servlets load. For a *clean local demo deployment only*, shut Tomcat down before removing stale `webapps\mham-auto` expanded directory; do not delete any user data directory. Keep `webapps\mham-auto.war` backed up before replacing it.
5. Browse:
   - Homepage: `http://localhost:8080/mham-auto/`
   - Old Phase 1.3 preview: `http://localhost:8080/mham-auto/catalog`
   - Real MySQL product listing: `http://localhost:8080/mham-auto/products`
   - Product details: click any product card
   - Phase 2 readiness: `http://localhost:8080/mham-auto/api/db-status`
6. Run after deployment:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase3.ps1' -SkipBuild
```

## Test checklist / definition of done

- [ ] Phase 3 offline Java check passes (malformed input, URL and image checks, currency guard).
- [ ] Maven build and all JUnit tests pass.
- [ ] `CatalogReadCli 4` passes non-destructive real-DB queries.
- [ ] `/products` displays DB rows, image links and SKU; search `q=wheel`, filter `vendor`, filter `category=uncategorized` and A–Z sort work.
- [ ] `/products?page=2` works with >=25 records, page links preserve filters.
- [ ] `/product?handle=<actual_handle>` shows its SKU, variant and CDN image; invalid handle returns 400; missing unpublished product returns 404.
- [ ] A dead image URL falls back to the local MHAM graphic without a JS alert.
- [ ] If source currency unconfirmed, price displays `Price pending verification` rather than KWD, and stock is labelled unverified.
- [ ] `/catalog`, the six original category cards, Retail/Trade switch and `/` still work.
- [ ] No real payments, orders, or actual stock commitments are enabled.

## Catalog behavior / validation

- Public listing includes only `active` and `published` products with at least one Kuwait-eligible variant. Explicitly excluded Kuwait variants are hidden.
- Search combines indexed FULLTEXT token-prefix title matching with indexed raw SKU prefix matching. SKU values retain leading apostrophes (as exported); a query with leading apostrophe can find them.
- Filters are vendor ID, category ID, or `uncategorized`, using prepared statements. Sort direction is restricted to an allowlist. Max page number is 10,000, page size 24, max search length 80 characters.
- `products` and `product` views use JSTL `<c:out>` for every imported field. Source HTML is reduced to escaped plain text; it is not rendered as trusted HTML.
- Images load directly from HTTPS `cdn.shopify.com` in the browser. Metadata and URLs only are stored in MySQL. Invalid URL/image fields fall back locally.
- The Phase 2 sample CSV contains only 4 products; a Phase 2 synthetic CSV can create 500 distinct test products. If imported alongside original records, total is 504, not 500.
- First/lowest verified variant price is shown for the list; unknown currency remains null. Precise KWD import is only allowed when source currency is confirmed.

## Limitations to report at viva

- No 1M-row benchmark was run in the delivery environment. Deep pagination, large vendor/category facet lists, multi-variant sorting and full-text search need load tests and query-plan review before a production release.
- The catalog covers source-product titles/SKUs; no verified fitment engine, sales price policy, stock reservation, cart or authentication. Those are later phases.
- Filtering facets return the first 200 alphabetic used vendors/categories. Product-detail variant listing is bounded to 100, images 24, to protect demo response sizes.
- A read-only product list cannot fix source SKU/image/alt-text issues; import admin governance is Phase 8.
- Request throttling, custom caching and advanced search are future production enhancements.

## Architecture

Browser → `/products`/`/product` Jakarta Servlet → `CatalogQuery` validation → `CatalogDao` with prepared JDBC queries → existing `DbPool`/MySQL → JSP view (`jakarta.tags.core` JSTL). Images are fetched by browser from approved CDN; no binary media is persisted.
