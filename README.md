# MHAM AUTO V2 — UI Finishing Overlay (November 2026)

**The latest change is strictly UI/UX.** See [`docs/V2_UI_AUDIT_AND_RELEASE.md`](docs/V2_UI_AUDIT_AND_RELEASE.md) for the complete audit, exact issues, tests and limitations, and [`docs/V2_UI_DEPLOYMENT.md`](docs/V2_UI_DEPLOYMENT.md) for Windows deployment and rollback. The Java backend, schema, routes, POM, and business rules are unchanged from the MHAM AUTO Pro source baseline. The included WAR is repackaged from previously compiled Java classes; prefer a fresh Maven build before deployment.

---

# MHAM AUTO | Pro Experience Release (November 2026)

Premium yellow-and-charcoal redesign of the existing **MHAM AUTO Java full-stack automotive spare parts marketplace**. The uploaded logo is used directly. This is a self-contained source release; the original project ZIP was kept unchanged during development.

## Architecture / decisions

- **Java 21 + Jakarta Servlet 6 + JSP/JSTL + JDBC + MySQL 8.x + Maven + Tomcat 10.1**. The original MVC backend, source importer, demo checkout, persistent cart, account management and B2B application logic remain in place.
- Responsive website and administrator dashboard use locally hosted CSS/vanilla JS; **no React migration and no third-party frontend build process**.
- Brand palette: charcoal/black, MHAM gold/yellow, warm neutral greys and white. Modern sans-serif throughout.
- Product images remain on the permitted Shopify CDN; no new image binaries are stored in MySQL.
- Existing source-backed order totals and demo inventory constraints are preserved. Real payments, actual delivery, stock reservation and verified vehicle fitment are **not provided**.

## What's new

1. New homepage with automotive art, logo branding, searching, six category entry points, live featured products loaded from the catalogue and clear demo disclosures.
2. New customer header, navigation, footer, catalogue cards, product detail gallery, account/cart/checkout styling, responsive mobile UI.
3. Browser-local saved parts `/wishlist` and recently viewed parts (requires JavaScript; **not** account-backed, not shared across devices).
4. New management sidebar, dashboard, real recent demo orders, customer count, quick links and responsive administrative tables.
5. ADMIN-only read-only customer directory `/admin/customers` with bounded prefix search and pagination.
6. Product management search by handle, name or SKU prefix, status filtering, thumbnails and archive action (soft deactivation keeps order history).
7. Category rename/delete for **administrator-created** categories only. Source-imported taxonomy stays immutable; category deletion requires no assigned products.
8. Admin orders and trade review preserve the original backend behavior; demo checkout flows remain demonstration-only.
9. Shared visual design system, keyboard focus styles, reduced-motion support, empty/error states and confirmation for destructive actions.

## Run locally on Windows

Requirements: Java 21, Maven 3.9.x, MySQL 8.x, Tomcat 10.1.x, Node.js optional for JS syntax checks.

### 1. Preserve the existing data

Back up your current MHAM AUTO directory and export the `mham_auto` database **before deployment**. Do not re-import synthetic CSV or rerun a full destructive DB setup over an existing installation. This release requires **no new SQL migration**; the existing V001–V006 schema must already be in place. For a fresh environment use `docs/PHASE_11_RELEASE_GUIDE.md`.

### 2. Set database credentials in the shell that launches Tomcat

```powershell
$env:MHAM_DB_USER = "YOUR_MYSQL_USER"
$env:MHAM_DB_PASSWORD = "YOUR_MYSQL_PASSWORD"
$env:MHAM_DB_URL = "jdbc:mysql://127.0.0.1:3306/mham_auto?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=UTC"
```

Credentials **must not** be committed or placed in `src/main/resources/mham-auto.properties`.

### 3. Build and test

```powershell
cd "D:\Java Project\MHAM AUTO"
mvn clean test package
```

This produces `target/mham-auto.war`. The source ZIP also includes the original test source and `scripts/verify_pro_release.py` static checks. The separately provided **precompiled WAR** is a convenience artifact; prefer your own Maven build for deployment and repeat live acceptance tests.

### 4. Deploy

Stop Tomcat, preserve your existing `webapps/mham-auto.war` and exploded folder as backups, then copy the newly built WAR:

```powershell
Copy-Item ".\target\mham-auto.war" "D:\Java Tools\apache-tomcat-10.1.61\webapps\mham-auto.war" -Force
& "D:\Java Tools\apache-tomcat-10.1.61\bin\startup.bat"
```

Ensure the old exploded `webapps/mham-auto` folder is removed or renamed **after backing it up** so Tomcat does not serve stale JSP/CSS. Start Tomcat from the shell where your database environment variables are set. Open `http://localhost:8080/mham-auto/`.

### 5. Verify the workflows

- Public: `/`, `/catalog`, `/products`, product detail, `/wishlist`, registration/login, persistent cart, addresses, demonstration checkout, orders, trade application.
- Admin: `/admin`, `/admin/products`, `/admin/product`, `/admin/categories`, `/admin/orders`, `/admin/customers`, `/admin/trade-applications`.
- Confirm unauthenticated/normal users cannot access `/admin/*`; verify CSRF and changes in a test database.
- Use `docs/PRO_ACCEPTANCE_CHECKLIST.md` to record browser, database and access-control results.

## Verified here vs outstanding

Modified Java source was compiled using Java 21 against the previous release classes and locally generated **compile-only Jakarta Servlet interface signatures** (Maven was not available in the execution environment). JavaScript syntax and static asset checks were performed. **Live Tomcat/JSP compilation, end-to-end MySQL flows, Maven JUnit and browser visual screenshots were not verified in this execution environment.** The precompiled WAR includes the original compiled dependency jars and the modified compiled Java classes, but must be tested on your machine before relying on it.

## Files

- `src/main/webapp/assets/css/premium.css`, `storefront.css` — common/admin and home design styles.
- `src/main/webapp/assets/images/mham-logo-3x.png` — logo cropped from the user's supplied image.
- `src/main/webapp/WEB-INF/views/fragments/admin-shell-*.jspf` — shared admin workspace.
- `src/main/java/com/mhamauto/servlet/FeaturedProductsServlet.java`, `WishlistServlet.java` — new read-only routes.
- `src/main/java/com/mhamauto/admin/*`, `src/main/java/com/mhamauto/servlet/AdminServlet.java` — bounded admin enhancements.
- `docs/legacy-frontend/` — archived unused homepage templates/CSS, retained for reference.
- Historic Phase 11 documentation and checks remain under `docs/` and `scripts/`.

This release targets a polished client/portfolio demo, **not** production payment and fulfilment readiness. See `docs/PRO_RELEASE_NOTES.md` for engineering changes and limitations.
