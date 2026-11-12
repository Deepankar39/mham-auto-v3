# MHAM AUTO — Academic V1

## Complete project README and phase-by-phase review handbook (Phases 4–11)

**Project:** MHAM AUTO — Automotive Spare Parts E-Commerce  
**Academic course:** MCA Semester I — Java and Web Programming Practical  
**Target market:** Kuwait | Retail (B2C) and a limited business-application (B2B) demonstration  
**Release:** Academic V1, Phase 11, **RC1 (release candidate)**  
**Document date:** 28 October 2026  
**Source baseline:** `MHAM_AUTO_AcademicV1_Full_Source_RC1.zip` and the phase-specific guides/manifests bundled in that archive

> **Release status — please read first:** The source packages through Phase 11 have been delivered and combined. Offline Java checks and structural checks have passed in the preparation environment. However, a complete Maven/JUnit build, MySQL integration, Tomcat/JSP deployment, authenticated browser scenarios and the 56-case acceptance matrix **have not been verified end-to-end in the preparation environment**. Nor has the user reported a complete Phase 4–11 acceptance run in this conversation. This is **RC1 / LOCAL ACCEPTANCE PENDING**, not a verified final production release.
>
> **Academic demonstration only.** The application does not take real payments, reserve real inventory, arrange delivery, provide wholesale credit/pricing, verify automotive fitment or authorize live transactions. The 500-product fixture is synthetic. The 1M+ catalogue target is an architectural goal, **not** a benchmarked capacity claim.

---

## Table of contents

1. [What the application contains](#1-what-the-application-contains)
2. [Phases and current status](#2-phases-and-current-status)
3. [Technology, versions and dependencies](#3-technology-versions-and-dependencies)
4. [Architecture and project layout](#4-architecture-and-project-layout)
5. [Phase 4 — Registration, login and authorization](#5-phase-4--registration-login-and-authorization)
6. [Phase 5 — Persistent shopping cart](#6-phase-5--persistent-shopping-cart)
7. [Phase 6 — Kuwait address book and demo checkout](#7-phase-6--kuwait-address-book-and-demo-checkout)
8. [Phase 7 — Customer dashboard and order history](#8-phase-7--customer-dashboard-and-order-history)
9. [Phase 8 — Admin dashboard and management](#9-phase-8--admin-dashboard-and-management)
10. [Phase 9 — B2B applications and review](#10-phase-9--b2b-applications-and-review)
11. [Phase 10 — UX, security and performance](#11-phase-10--ux-security-and-performance)
12. [Phase 11 — Release validation and academic hand-in](#12-phase-11--release-validation-and-academic-hand-in)
13. [MySQL schema and migrations](#13-mysql-schema-and-migrations)
14. [Installation — existing project](#14-installation--existing-project)
15. [Installation — fresh source and database](#15-installation--fresh-source-and-database)
16. [Build, deployment and test commands](#16-build-deployment-and-test-commands)
17. [Full manual acceptance journeys](#17-full-manual-acceptance-journeys)
18. [CSV, images, pricing and scalability](#18-csv-images-pricing-and-scalability)
19. [Security and known limitations](#19-security-and-known-limitations)
20. [Academic syllabus, screenshots and report](#20-academic-syllabus-screenshots-and-report)
21. [Troubleshooting and recovery](#21-troubleshooting-and-recovery)
22. [Final release checklist and reference documents](#22-final-release-checklist-and-reference-documents)

---

## 1. What the application contains

MHAM AUTO is a Java 21 web application packaged as a Maven WAR and deployed on Apache Tomcat. JSPs render server-side HTML. Java Servlets receive requests, filters enforce request/session rules, services apply business logic, and JDBC DAOs use MySQL for persistent records. CSS/vanilla JavaScript provide a responsive interface.

**Already established before Phase 4 (Phases 1.1–3):** development environment; Maven WAR; JSP homepage; `/catalog` Retail/Trade preview; Java Servlet → Service → JSP integration; MySQL/JDBC and 74-column Shopify CSV importer; normalized products/variants/images; live MySQL-backed `/products` and `/product?handle=...` views. This README focuses on **Phases 4–11** and assumes that foundation.

**Customer experience:** browse parts → create account/sign in → choose a variant → manage persisted cart → save a *fictional* Kuwait address → review demo total → place a no-payment demo order → see immutable confirmation, order history and account profile.

**Administrator experience:** sign in with a separately granted `ADMIN` role → view admin dashboard → maintain products/categories/sample stock/fictional demo prices → review demo orders → review business-account applications. Administrative updates require server-side authorization and CSRF validation.

**Trade experience:** a normal signed-in customer submits a company application and sees `PENDING`, `APPROVED` or `REJECTED` after review. Approval is **not** a wholesale entitlement or a customer-role change.

### Important application URLs

All URLs are relative to `http://localhost:8080/mham-auto`.

| Area | Path | Audience |
|---|---|---|
| Homepage / preview | `/`, `/catalog` | Public |
| Live products / details | `/products`, `/product?handle=...` | Public |
| Registration / login | `/register`, `/login` | Public |
| Logout | `/logout` (POST form) | Signed in |
| Account / orders | `/account`, `/orders`, `/orders/view?ref=...` | Customer, own data |
| Cart / cart counter | `/cart`, `/api/cart/count` | Customer; count is safe for guests |
| Address / checkout | `/addresses`, `/checkout` | Customer |
| Order confirmation | `/order-confirmation?ref=DEMO-...` | Order owner |
| Admin dashboard | `/admin` | ADMIN |
| Admin catalogue | `/admin/products`, `/admin/product`, `/admin/categories` | ADMIN |
| Admin orders | `/admin/orders`, `/admin/order?id=...` | ADMIN |
| Business application | `/trade/apply`, `/trade/status` | Customer |
| Trade administration | `/admin/trade-applications`, `/admin/trade-application?id=...` | ADMIN |
| Diagnostics | `/health`, `/api/db-status` | Local verification |

Use a real handle/order reference from your local database; the placeholders above are not literal working IDs.

## 2. Phases and current status

| Phase | Scope | Source included in RC1? | Acceptance status |
|---|---|---|---|
| 1.1–1.4 | Environment, homepage, preview, shared foundation | Yes | Historical work; earlier foundation reported working |
| 2 | MySQL/JDBC, Shopify CSV import | Yes | User explicitly reported testing Phase 2 |
| 3 | Database-backed catalogue | Yes | Source delivered; confirm integrated tests locally |
| **4** | Registration, login, sessions, roles | **Yes** | Local integrated acceptance not evidenced |
| **5** | Persistent customer cart | **Yes** | Local integrated acceptance not evidenced |
| **6** | Kuwait address + demo checkout | **Yes** | Local integrated acceptance not evidenced |
| **7** | Profile, dashboard, order history | **Yes** | Local integrated acceptance not evidenced |
| **8** | Admin CRUD, sample inventory, demo order review | **Yes** | Local integrated acceptance not evidenced |
| **9** | Business application + admin review | **Yes** | Local integrated acceptance not evidenced |
| **10** | Accessibility, headers, client UX, query safeguards | **Yes** | Local integrated acceptance not evidenced |
| **11** | Complete-source RC1, automated gates, docs, report and evidence plan | **Yes** | Final release acceptance pending |

**Phases remaining to develop under the frozen Academic V1 plan: 0.**  
**Remaining work: execute tests, fix any integration defects, capture genuine evidence, complete the report, and sign off the release.** Do not interpret a delivered ZIP or a passed standalone Java syntax check as proof of a live working application.

## 3. Technology, versions and dependencies

### Local toolchain

| Component | Project version / role |
|---|---|
| OS / IDE | Windows; Visual Studio Code |
| Java | JDK **21** |
| Maven | **3.9.11** target environment |
| Apache Tomcat | **10.1.61** |
| Servlet API | Jakarta Servlet **6.0.0** (Tomcat-provided) |
| Views | JSP and Jakarta JSTL |
| Database | MySQL **8.x**, InnoDB, MySQL Workbench |
| Frontend | HTML5, CSS3, vanilla JavaScript, local SVG assets |
| Application structure | MVC-style Servlets → Services → DAOs → JDBC/MySQL |
| Packaging | Maven `war`; result: `target/mham-auto.war` |
| Test utilities | JUnit 5, Java offline tests, PowerShell scripts; optional Python/Node checks |

### Actual Maven dependencies in `pom.xml`

| Artifact | Version | Purpose |
|---|---:|---|
| `jakarta.servlet:jakarta.servlet-api` | 6.0.0 | Servlet types; `provided` scope |
| `jakarta.servlet.jsp.jstl:jakarta.servlet.jsp.jstl-api` | 3.0.0 | JSTL API |
| `org.glassfish.web:jakarta.servlet.jsp.jstl` | 3.0.1 | JSTL implementation |
| `com.mysql:mysql-connector-j` | 9.4.0 | MySQL JDBC driver |
| `com.zaxxer:HikariCP` | 6.3.0 | Bounded JDBC connection pool |
| `org.apache.commons:commons-csv` | 1.14.1 | Shopify CSV parsing |
| `org.slf4j:slf4j-simple` | 2.0.17 | Logging backend |
| `org.junit.jupiter:junit-jupiter` | 5.12.2 | Unit testing (`test` scope) |
| `maven-compiler-plugin` | 3.14.1 | Compile for Java 21 |
| `maven-war-plugin` | 3.4.0 | Build WAR |
| `maven-surefire-plugin` | 3.5.3 | Execute tests |

These are the versions pinned in the **RC1 source**, not promises that Maven Central resolution or compilation has been verified on your PC. Verify with `java -version` and `mvn -version`.

## 4. Architecture and project layout

```text
Browser (HTML / JSP-rendered pages, CSS, JS)
  -> Tomcat 10.1 / Servlet filters
  -> Java Servlet controllers
  -> domain rules / services
  -> JDBC DAOs -> HikariCP -> MySQL (mham_auto)
Browser -> HTTPS Shopify CDN (product image URLs only)
Local Shopify CSV -> Java import CLI -> JDBC DAO -> MySQL
```

All price, authorization and order decisions are server-side. JSP pages should not execute SQL or decide administrator privileges.

### Principal directories

```text
D:\Java Project\MHAM AUTO\
├── pom.xml
├── README.md                       <-- this comprehensive replacement README
├── database\                       <-- V001–V006 + read-only review SQL
├── docs\                           <-- per-phase guides/manifests, ERD, reports
├── evidence\                       <-- screenshot/results templates
├── samples\                        <-- 4-row Shopify CSV + synthetic 500-row CSV
├── scripts\                        <-- Phase 1–11 PowerShell checks
├── tests\                          <-- standalone Java smoke/validation checks
└── src\
    ├── main\
    │   ├── java\com\mhamauto\
    │   │   ├── auth\               <-- Phase 4 identity / sessions / CSRF
    │   │   ├── cart\               <-- Phase 5 persistence and rules
    │   │   ├── checkout\           <-- Phase 6 addresses and orders
    │   │   ├── account\            <-- Phase 7 dashboard/history
    │   │   ├── admin\              <-- Phase 8 admin management
    │   │   ├── trade\              <-- Phase 9 applications
    │   │   ├── catalog\            <-- Phase 3 product reads
    │   │   ├── importer\           <-- Phase 2 Shopify importer
    │   │   ├── db\                 <-- pool/config/migrations
    │   │   ├── dao\                <-- import DAO
    │   │   ├── filter\             <-- protected pages / request defaults
    │   │   ├── servlet\            <-- HTTP route controllers
    │   │   └── web\                <-- common view/security helpers
    │   ├── resources\mham-auto.properties
    │   └── webapp\
    │       ├── index.jsp
    │       ├── assets\css\, assets\js\, assets\images\
    │       └── WEB-INF\views\      <-- JSPs and reusable fragments
    └── test\java\                  <-- JUnit test sources
```

The complete source archive also includes the original phase documentation and the final report (`docs/MHAM_AUTO_AcademicV1_Project_Report.docx` / `.pdf`). See `docs/PHASE_11_ARCHITECTURE.md` and `docs/PHASE_11_ERD.mmd` for a more detailed system view.

---

## 5. Phase 4 — Registration, login and authorization

**Goal:** introduce real customer identity, login/logout sessions and a protected administrator role without breaking public browsing.

### Implemented features

- Registration with name, normalized email, password and repeated-password validation; duplicate email rejection.
- Login against MySQL records; salted **PBKDF2-HMAC-SHA256** password hashes, not plaintext storage.
- Server-side `HttpSession` authentication; session rotation on successful login; logout invalidates the session.
- Configured 30-minute idle session timeout, cookie-only session tracking and `HttpOnly` session cookie in `WEB-INF/web.xml`.
- Roles `CUSTOMER` and `ADMIN`. Public registration assigns **CUSTOMER only**.
- `ProtectedPagesFilter` revalidates logged-in account status and roles against MySQL; unauthorized admin access is rejected.
- Synchronizer CSRF tokens for state-changing forms and basic process-local login throttling.
- Registration/login pages and early `/account` dashboard; Phase 4 `/admin` is only an access-control entry, not full management.

### Main changes

- **V002** migration: `database/03_identity_schema.sql` creates `users`, `roles`, `user_roles`; `database/04_phase4_validation.sql` is read-only.
- Java: `auth/IdentityRules.java`, `PasswordHasher.java`, `AuthService.java`, `AuthDao.java`, `SessionAuth.java`, `Csrf.java`, `LoginThrottle.java`; `filter/ProtectedPagesFilter.java`; `servlet/IdentityServlet.java` and `AccountServlet.java`.
- CLI: `com.mhamauto.cli.IdentityCli` for migrate/status and explicit local admin bootstrap.
- JSP: `login.jsp`, `register.jsp`, initial `account.jsp`, `admin-entry.jsp`; shared navigation updated. Assets: `auth.css`, `auth.js`.
- Configuration: `WEB-INF/web.xml` session policy; `META-INF/context.xml`.

### Routes and testing

- `GET/POST /register`; `GET/POST /login`; `POST /logout`; protected `GET /account`; admin-only `GET /admin`.
- Offline check: `scripts/Test-Phase4-Offline.ps1`.
- Live check: `scripts/Test-Phase4.ps1 -SkipBuild` (requires built classes and deployed WAR; creates a uniquely named `.test` customer).
- JUnit examples: `IdentityRulesTest`, `PasswordHasherTest`, `LoginThrottleTest`.
- **Manual acceptance:** duplicate email rejected; bad password not accepted; session persists only while logged in; logout revokes access; CUSTOMER gets 403 on admin; missing/invalid CSRF gets 403; an explicit local admin promotion is effective only after refreshed login.

**Definition of done:** customer authentication and role separation work against MySQL and earlier pages still work. Final acceptance remains subject to your local evidence.

## 6. Phase 5 — Persistent shopping cart

**Goal:** allow each authenticated customer to keep a shopping cart across sessions using MySQL.

### Implemented features

- Add an existing **variant** to cart; update quantity; remove one line; clear the entire cart.
- Quantity range **1–20** per line, validated on the server and by MySQL constraints; conflicts can return HTTP 409.
- Only one cart per user; each cart contains one row per variant, with uniqueness enforced in MySQL.
- Persistent cart survives logout/login; other customers cannot see or change its contents.
- Cart count endpoint and navigation badge (`/api/cart/count`); anonymous count is zero.
- Price and availability remain server-authoritative. If imported source currency is unknown, the item can stay in cart but the KWD price/total is shown as **pending**.
- Product detail page has an add-to-cart form; cart has responsive quantity/remove/clear controls and empty state.
- No payment, order placement or inventory reservation.

### Main changes

- **V003** migration: `database/05_cart_schema.sql` creates `carts`, `cart_items`; `database/06_phase5_validation.sql` provides optional read-only checks.
- Java: `cart/CartRules.java`, `CartModels.java`, `CartDao.java`, `CartService.java`, `CartConflict.java`; `servlet/CartServlet.java`, `CartCountServlet.java`; `db/CartMigration.java`; `cli/CartCli.java`.
- JSP/assets: `cart.jsp`, `cart.css`, `cart.js`, `cart-badge.js`; product detail, navigation and the route filter were incrementally updated.

### Routes and testing

- `GET/POST /cart`; `GET /api/cart/count`; product-detail add-to-cart action.
- Offline: `scripts/Test-Phase5-Offline.ps1`.
- Live: `scripts/Test-Phase5.ps1 -SkipBuild` (creates two disposable customer accounts and changes their test cart rows).
- JUnit: `CartRulesTest`.
- **Manual acceptance:** item added, quantity changed, quantity 21 rejected, line removed, cart cleared, count updated, session restart preserves cart, other user sees an empty cart, invalid CSRF rejected, unavailable/inactive variant cannot be added.

**Definition of done:** correct transactional user-scoped cart persistence with no assumed KWD price.

## 7. Phase 6 — Kuwait address book and demo checkout

**Goal:** create durable **simulated** orders from a customer cart, using a validated Kuwait address, without any real purchase.

### Implemented features

- Add/edit/delete **up to ten** saved Kuwait addresses per account. Fields include governorate, area/address and Kuwait phone validation.
- Recognize Kuwait's six governorates (Capital, Hawalli, Farwaniya, Ahmadi, Jahra, Mubarak Al Kabeer).
- Checkout review checks active products, source/sample availability, authenticated user, owner-scoped selected address and **verified or explicitly fictional KWD pricing**.
- Reject checkout for empty/unavailable/unpriced carts; never trust a client-submitted total.
- Transactionally create `demo_orders` and `demo_order_items`; take immutable name/SKU/quantity/unit-price/currency/address snapshots; clear that user's cart on success.
- Unique checkout request tokens make repeated submissions idempotent, returning the original demo reference instead of duplicating the order.
- Order confirmation is scoped to the order owner; **no payment, live stock deduction, reservation or delivery record**.
- `demo_price_assignments` supports *explicitly fictional* KWD prices for synthetic academic test products, retaining price provenance.

### Main changes

- **V004** migration: `database/07_checkout_schema.sql` creates `customer_addresses`, `demo_orders`, `demo_order_items`, `demo_price_assignments`.
- Java: `checkout/AddressRules.java`, `AddressService.java`, `AddressDao.java`, `CheckoutRules.java`, `CheckoutService.java`, `CheckoutDao.java`, `CheckoutToken.java`, `CheckoutModels.java`, `CheckoutConflict.java`.
- Servlets: `AddressServlet.java`, `CheckoutServlet.java`. CLI: `CheckoutCli.java`, `DemoPriceCli.java`. Filter, account page and cart page updated.
- JSPs: `addresses.jsp`, `checkout.jsp`, `order-confirmation.jsp` and `checkout.css`.

### Routes and testing

- `GET/POST /addresses`; `GET/POST /checkout`; `GET /order-confirmation?ref=...`.
- Offline: `scripts/Test-Phase6-Offline.ps1`.
- Live: `scripts/Test-Phase6.ps1 -SkipBuild` (creates test accounts, a fictional address and a demo order; requires checkout-eligible test product pricing).
- JUnit: `AddressRulesTest`, `CheckoutRulesTest`.
- **Manual acceptance:** invalid governorate/phone rejected; cross-user address access denied; missing CSRF rejected; unverified source prices block checkout; successful order persists one time; duplicate token does not double-order; order snapshots do not change after source changes; cart is emptied only on success; no real financial/stock effects.

**Definition of done:** repeatable, idempotent and clearly labelled *NO PAYMENT* order journey.

## 8. Phase 7 — Customer dashboard and order history

**Goal:** provide a usable MySQL-backed account portal and persistent, owner-scoped order list.

### Implemented features

- Customer `/account` dashboard with greeting, account-related counts and five most recent demo orders.
- Change display name using a CSRF-protected POST; email remains read-only.
- `/orders` list ordered newest first, **12 entries per page** with bounded pagination.
- `/orders/view?ref=...` shows immutable order line and address snapshots from Phase 6.
- Links to saved Kuwait addresses and cart; accurate empty states and responsive layouts.
- Access to another customer's order reference returns 404; invalid pages/references are rejected.
- Demo order totals are historical **simulated totals**, not revenue.

### Main changes

- **No new database migration**; reuses V002 users and V004 order/address tables and index on order user/date.
- Java: `account/PortalDao.java`, `PortalModels.java`, `PortalRules.java`, `servlet/CustomerOrdersServlet.java`; updated `AccountServlet.java`, `ProtectedPagesFilter.java`.
- JSPs: updated `account.jsp`, `order-confirmation.jsp`, navigation; new `orders.jsp`, `order-detail.jsp`; `customer-portal.css`.
- SQL: `database/09_phase7_validation.sql` is a **read-only** check, not DDL.

### Routes and testing

- `GET /account`; `POST /account` with `action=updateName`; `GET /orders?page=1`; `GET /orders/view?ref=...`.
- Offline: `scripts/Test-Phase7-Offline.ps1`.
- Live: `scripts/Test-Phase7.ps1 -SkipBuild` (creates a test user and validates empty-state, rename, input/CSRF checks; does **not** replace manual placed-order journey).
- JUnit: `PortalRulesTest`.
- **Manual acceptance:** change display name, log out/in and confirm persistence; create a demo order; see it in recent/history/details; change current product/address and verify old snapshots remain fixed; Customer B cannot see Customer A's order.

**Definition of done:** account details and order history display only data belonging to the logged-in user.

## 9. Phase 8 — Admin dashboard and management

**Goal:** provide limited, properly authorized academic administration without rewriting source catalogue relations or historic orders.

### Implemented features

- Dashboard counts for products, variants, sample inventory, categories and demo orders.
- Product list with filtering and **20-item pagination**; create/edit/archive products; no hard deletion of historical products.
- Unique handle and SKU validation; admin edits the **first variant** only. Complex multi-variant management remains with the Shopify CSV importer.
- Category creation/assignment; sample stock updates; explicitly fictional KWD demo prices (with provenance), not automatic real-price conversion.
- Admin view of demo orders and controlled simulated status updates (`DEMO_PLACED` → permitted reviewed/cancelled demo states).
- Historical order snapshots are never recalculated when products are renamed or repriced.
- Admin changes logged as `admin_events` with actor, action, target and summary.
- Server rechecks `ADMIN` permission and CSRF on admin writes; product image URLs must satisfy the existing Shopify CDN HTTPS policy.

### Main changes

- **V005** migration: `database/10_admin_schema.sql` adds `admin_events`. Read-only validation: `database/11_phase8_validation.sql`.
- Java: `admin/AdminDao.java`, `AdminModels.java`, `AdminRules.java`; `servlet/AdminServlet.java`; `db/AdminMigration.java`; `cli/AdminCli.java`.
- JSPs: `admin-dashboard.jsp`, `admin-products.jsp`, `admin-product-form.jsp`, `admin-categories.jsp`, `admin-orders.jsp`, `admin-order.jsp`, reusable admin fragments; `admin.css`.
- Incremental changes to earlier product/cart/account JSP text preserve demo disclaimers and admin route ownership.

### Routes and testing

- `/admin`, `/admin/products`, `/admin/product`, `/admin/categories`, `/admin/orders`, `/admin/order?id=...`.
- Offline: `scripts/Test-Phase8-Offline.ps1`.
- Live: `scripts/Test-Phase8.ps1 -SkipBuild` (**public pages and anonymous admin redirect only**; does not perform admin CRUD).
- JUnit: `AdminRulesTest`.
- **Manual acceptance:** customer gets 403 on admin pages; admin creates category and unique product, edits inventory/demo price, sees product publicly, creates demo order through a customer, reviews order, verifies prior snapshots are unchanged, validates duplicate handle/SKU failure and CSRF rejection.

**Definition of done:** secure basic admin management with source-preserving limits and audit trail.

## 10. Phase 9 — B2B applications and review

**Goal:** implement only the frozen Academic V1 B2B scope: a company application and simulated administrator decision.

### Implemented features

- One application per logged-in customer with company name/contact, business type, Kuwait governorate/phone and notes.
- Allowed sample business types: `GARAGE`, `WORKSHOP`, `PARTS_RETAILER`, `FLEET`, `OTHER`.
- New applications start `PENDING`; customer sees only their own submission and status.
- ADMIN can list/filter applications, open details and make a **single** review decision: `PENDING` → `APPROVED` or `REJECTED`.
- Decision and submission events recorded in a separate history table; review uses transaction and row locking.
- Private admin review notes are not exposed to applicants. Ordinary customer access to admin review routes is forbidden.
- **Approval does not modify `user_roles`, unlock trade prices, create credit limits or enable wholesale/RFQ ordering.**

### Main changes

- **V006** migration: `database/12_trade_application_schema.sql` creates `business_applications` and `business_application_events`; `database/13_phase9_validation.sql` is read-only.
- Java: `trade/TradeRules.java`, `TradeModels.java`, `TradeDao.java`; `servlet/TradeApplicationServlet.java`, `AdminTradeServlet.java`; `db/TradeMigration.java`; `cli/TradeCli.java`.
- JSPs: `trade-apply.jsp`, `trade-status.jsp`, `admin-trade-applications.jsp`, `admin-trade-application.jsp`; `trade.css` and nav/filter/account links.

### Routes and testing

- Customer: `GET/POST /trade/apply`, `GET /trade/status`; ADMIN: `/admin/trade-applications`, `/admin/trade-application?id=...`.
- Offline: `scripts/Test-Phase9-Offline.ps1`.
- Live: `scripts/Test-Phase9.ps1 -SkipBuild` (**public and anonymous route checks only**; authenticated trade review requires manual UAT).
- JUnit: `TradeRulesTest`.
- **Manual acceptance:** Customer A submits, sees PENDING; repeat submission rejected; Customer B only sees B's application; admin approves A and rejects B; applicant sees status but not private notes; a second review is blocked; no role or price privileges change.

**Definition of done:** one-way, auditable trade-application review without wholesale operations.

## 11. Phase 10 — UX, security and performance

**Goal:** improve integrated user experience and defense-in-depth without adding new commerce functionality.

### Implemented improvements

- Keyboard focus states, skip links, responsive forms, improved mobile navigation, scroll-safe admin tables, print styles and `prefers-reduced-motion` support.
- More accurate current-page navigation; mobile menu closes on Escape, outside click and resize; accessible cart count announcements.
- Adds/refines HTTP `Content-Security-Policy`, `Permissions-Policy`, `X-Content-Type-Options: nosniff`, frame protection and other response headers.
- Blocks unsafe browser requests with an explicitly mismatched `Origin`; existing CSRF tokens still remain mandatory. Missing Origin is not treated as proof of trust.
- CSP allows approved self-hosted JS/CSS and product images from `https://cdn.shopify.com`; prohibits legacy inline script/style patterns.
- Public static assets use a short revalidatable **300-second** cache; sensitive responses stay non-cacheable.
- Adds **10-second** timeout to prepared catalogue reads and a read-only `EXPLAIN` review query set.
- Fixes admin list empty-state logic and catalogue navigation highlights.

### Main changes

- **No MySQL migration.** Read-only performance SQL: `database/14_phase10_performance_review.sql`.
- Java: `web/RequestSecurity.java`; updated `filter/RequestDefaultsFilter.java`, `catalog/CatalogDao.java`.
- Web: `assets/css/phase10.css`, `assets/js/phase10.js`, shared header/JSP fragments and selected legacy JS assets.
- Tests: `RequestSecurityTest`, `RequestDefaultsFilterTest`, `tests/Phase10OfflineCheck.java`; performance helper `scripts/Measure-Phase10.ps1`.

### Testing

- Offline: `scripts/Test-Phase10-Offline.ps1`.
- Live: `scripts/Test-Phase10.ps1 -SkipBuild` (basic security headers, public/protected pages, Origin rejection and static-asset cache behavior).
- Performance sample: `scripts/Measure-Phase10.ps1 -Runs 5` for local `/`, `/catalog`, `/products`, `/health` response times.
- **Manual acceptance:** mobile+desktop layouts, keyboard/zoom/reduced-motion, image CSP compatibility, absence of unexpected browser console errors, complete Phase 4–9 functional regression, account isolation and admin authorization.

**Definition of done:** earlier flows still work with the new headers, styles and request safeguards. **Not** a production penetration test or 1M-record load test.

## 12. Phase 11 — Release validation and academic hand-in

**Goal:** combine the approved Academic V1 layers into a verifiable source release and provide documentation and evidence templates.

### Deliverables produced

- `MHAM_AUTO_AcademicV1_Full_Source_RC1.zip`: complete **source** tree, not compiled WAR.
- `MHAM_AUTO_Phase11_Release_Overlay.zip`: additive Phase 11 documentation/tests over an existing Phase 10 project.
- Full architecture docs, `docs/PHASE_11_ERD.mmd`, database dictionary, SQL verification, source provenance and known-limitations register.
- `docs/PHASE_11_SYLLABUS_MAPPING.md`: the project's Java/Web coursework traceability.
- `docs/MHAM_AUTO_AcademicV1_Project_Report.docx` and `.pdf`: editable and printable draft academic report (7 pages; edit identity/screenshots).
- `docs/PHASE_11_TEST_MATRIX.csv`: **56** case acceptance matrix; statuses start `NOT RUN`.
- `docs/PHASE_11_EVIDENCE_CHECKLIST.md`: named screenshot requirements; optional evidence capture script.
- `scripts/Test-Phase11-Offline.ps1`, `Test-Phase11-Database.ps1`, `Test-Phase11-Live.ps1`, `Collect-Phase11-Evidence.ps1`, `phase11_static_audit.py`.

### What the checks actually do

- **Offline gate:** invokes 11 historical plain-Java checks (Phases 1.3, 1.4 and 2–10), plus Python static audit **if Python is available** and JavaScript syntax checks **if Node is available**. Optional tools may be skipped; record any skip before accepting the release.
- **Maven/JUnit gate:** compiles and runs the actual Maven test suite and creates a WAR. **Must be run locally; no verified result is included here.**
- **Database gate:** six CLI status/statistics checks (catalogue, identity, cart, checkout, admin, trade). Separately execute `database/15_phase11_read_only_validation.sql` in Workbench.
- **HTTP gate:** public route responses, basic response headers, anonymous redirects and forged Origin rejection. **It does not log in as an administrator or complete checkout.**
- **Manual/UAT gate:** use two customer accounts and an admin to test authenticated and cross-account workflows; record observed results in the CSV.
- **Release evidence:** screenshot/logs saved under `evidence/`; no generated screenshot should be represented as a real browser test.

### Current verified vs unverified state

**Passed in preparation environment:** original source reconstruction, 11/11 standalone Java regression checks, the source static audit (74-column CSV/500 synthetic rows/schema table count/Servlet mappings), JavaScript syntax checks, archive integrity, and byte-for-byte preservation of earlier application paths by the Phase 11 overlay.

**Not yet established:** full Maven/JUnit success, live MySQL migration and transactions, compiled JSP pages in Tomcat, authenticated customer/admin/B2B workflows, screenshot evidence, and 1M+ product throughput.

**Definition of done:** build+database+HTTP gates pass, all mandatory manual tests pass, report/screenshots are authentic, known defects are resolved or documented, and Academic V1 is signed off. Until then the release remains **RC1**.

---

## 13. MySQL schema and migrations

**Database:** `mham_auto`  
**Engine:** MySQL 8 / InnoDB  
**Tables:** **26 application tables** across V001–V006, plus the `schema_migrations` tracking table.  
**IDs:** `BIGINT` internal IDs; raw Shopify SKUs/handles remain separate business identifiers.

| Order | Introduced | Migration SQL | Tables created |
|---|---|---|---|
| V001 | Phase 2 | `database/01_catalog_schema.sql` | 14 catalogue, import, variant and media tables |
| V002 | Phase 4 | `database/03_identity_schema.sql` | `users`, `roles`, `user_roles` |
| V003 | Phase 5 | `database/05_cart_schema.sql` | `carts`, `cart_items` |
| V004 | Phase 6 | `database/07_checkout_schema.sql` | `customer_addresses`, `demo_orders`, `demo_order_items`, `demo_price_assignments` |
| V005 | Phase 8 | `database/10_admin_schema.sql` | `admin_events` |
| V006 | Phase 9 | `database/12_trade_application_schema.sql` | `business_applications`, `business_application_events` |
| None | Phase 7 / 10 / 11 | Validation/read-only SQL only | No new app tables |

The V001 tables cover vendors, categories, import jobs, products, variants, options, variant-option values, inventory, media assets, product images, variant images, regional variant availability, tags and import errors.

### Integrity and data ownership

- Unique product handles and source SKU constraints prevent duplicate identities.
- Media assets are URL/checksum references; many product/image associations may reuse one asset. Image **bytes are not stored in MySQL**.
- Cart/addresses/order history are scoped to the signed-in customer.
- `demo_orders` is uniquely constrained by order reference and `(user_id, checkout_token)`.
- Demo order item and address snapshots preserve historical facts even after admin edits.
- Business approval is separate from `user_roles`; `APPROVED` does not create an admin/trade role.
- Admin/trade events preserve basic review history.
- MySQL migrations are version/checksum tracked. A conflicting checksum must be investigated; **never change an applied migration SQL file to make it pass**.

### Non-destructive database review

Execute `database/15_phase11_read_only_validation.sql` in Workbench **after** the six versioned migrations are complete. Other optional review files are `04_phase4_validation.sql`, `06_phase5_validation.sql`, `08_phase6_validation.sql`, `09_phase7_validation.sql`, `11_phase8_validation.sql`, `13_phase9_validation.sql`, `14_phase10_performance_review.sql`.

**Do not** delete the database, rerun destructive seed scripts, or reset migration tracking to fix application errors. Back up data before schema work.

## 14. Installation — existing project

Use this method when you already have the project at `D:\Java Project\MHAM AUTO` and have applied Phases 1–10. For documentation only, **you do not need to re-extract prior phase ZIPs**.

To replace the original short `README.md` with this file, first make a copy:

```powershell
$project = 'D:\Java Project\MHAM AUTO'
Set-Location -LiteralPath $project
Copy-Item -LiteralPath '.\README.md' -Destination '.\README_RC1_Backup.md' -Force
# Download the new README.md and copy it to the project root:
Copy-Item -LiteralPath "$env:USERPROFILE\Downloads\README.md" -Destination '.\README.md' -Force
```

If Phase 11's documentation/test overlay is **not** installed, download it separately; copying this README alone does **not** add Phase 11 scripts or reports. Before applying the Phase 11 overlay, back up the source directory and **export MySQL from Workbench**. The overlay is intended for the existing project root, not a separate Maven project.

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = Join-Path $env:USERPROFILE 'Downloads\MHAM_AUTO_Phase11_Release_Overlay.zip'
$backup = 'D:\Java Project\MHAM_AUTO_PrePhase11_' + (Get-Date -Format yyyyMMdd_HHmmss)
Copy-Item -LiteralPath $project -Destination $backup -Recurse -Force
Expand-Archive -LiteralPath $zip -DestinationPath $project -Force
Set-Location -LiteralPath $project
```

If this README is in a small README-only ZIP, extract **that** package after Phase 11, or copy this file manually, so the comprehensive version replaces the original RC1 README.

## 15. Installation — fresh source and database

Only use this method for a **new** local working directory or a clean test environment. Do not overwrite the actively used database with a fresh-install example.

### Step A — Verify tools

```powershell
java -version
javac -version
mvn -version
```

Install/configure Java 21, Maven 3.9.11, MySQL 8.x/Workbench and Tomcat 10.1.61 if absent. Open the folder in VS Code after extraction.

### Step B — Extract the complete source

```powershell
$project = 'D:\Java Project\MHAM AUTO'
$zip = Join-Path $env:USERPROFILE 'Downloads\MHAM_AUTO_AcademicV1_Full_Source_RC1.zip'
if (Test-Path -LiteralPath $project) {
    throw 'Project path already exists; choose a fresh path or back it up deliberately.'
}
New-Item -ItemType Directory -Path $project -Force | Out-Null
Expand-Archive -LiteralPath $zip -DestinationPath $project
Set-Location -LiteralPath $project
```

### Step C — Create the MySQL database and local user

1. Open MySQL Workbench with an administrator connection.
2. Execute `database/00_create_database.sql` to create `mham_auto` with `utf8mb4`.
3. Create a **separate local** MySQL account; do not use the root account for the application. For example (replace the placeholder with a private strong password in Workbench, not in your source files):

```sql
CREATE USER IF NOT EXISTS 'mham_app'@'127.0.0.1'
  IDENTIFIED BY 'REPLACE_WITH_PRIVATE_LOCAL_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES
  ON mham_auto.* TO 'mham_app'@'127.0.0.1';
```

The example grants are for **local academic setup and migrations**, not a recommended permanent production least-privilege role. Check the MySQL host/account configuration if your server resolves the connection differently. Never share the password in a screenshot or chat log.

### Step D — Configure the database connection in PowerShell

```powershell
$env:MHAM_DB_USER = 'mham_app'
$secret = Read-Host 'Enter local MySQL application password' -AsSecureString
$pointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($secret)
try {
    $env:MHAM_DB_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
}
finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
}
Remove-Variable secret,pointer

# Optional override; normally leave unset for localhost:
# $env:MHAM_DB_URL = 'jdbc:mysql://127.0.0.1:3306/mham_auto?sslMode=DISABLED&allowPublicKeyRetrieval=true&connectionTimeZone=UTC'
```

The application defaults to a local `mham_auto` JDBC URL with TLS **disabled**, so it must not be directly reused on a public production server. Environment variables apply to the **current terminal and its child processes**. If Tomcat runs as a Windows service, configure the service process separately rather than assuming it inherits these interactive variables.

### Step E — Build and apply migrations V001 → V006

```powershell
Set-Location -LiteralPath 'D:\Java Project\MHAM AUTO'

mvn clean test package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven/JUnit compilation failed' }

$cp = 'target/classes;target/dependency/*'
$migrations = @(
    'com.mhamauto.cli.CatalogCli',  # V001
    'com.mhamauto.cli.IdentityCli', # V002
    'com.mhamauto.cli.CartCli',     # V003
    'com.mhamauto.cli.CheckoutCli', # V004
    'com.mhamauto.cli.AdminCli',    # V005
    'com.mhamauto.cli.TradeCli'     # V006
)
foreach ($cli in $migrations) {
    & java -cp $cp $cli migrate
    if ($LASTEXITCODE -ne 0) { throw "Migration failed: $cli" }
}
```

Do not attempt Phase 9 migration before identity/admin migrations. Applied versions should be recognized on re-run by checksum. A failed DDL operation must be diagnosed and recovered from backup, not blindly undone.

### Step F — Seed **only** a new empty test database

```powershell
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.CatalogCli dry-run '--file=samples/product data sample.csv'
if ($LASTEXITCODE -ne 0) { throw 'Sample CSV validation failed' }
java -cp $cp com.mhamauto.cli.CatalogCli import '--file=samples/product data sample.csv'
if ($LASTEXITCODE -ne 0) { throw 'Sample import failed' }

# Optional synthetic data for testing, not real commercial inventory:
java -cp $cp com.mhamauto.cli.CatalogCli import '--file=samples/SYNTHETIC_TEST_500.csv'
if ($LASTEXITCODE -ne 0) { throw 'Synthetic import failed' }

java -cp $cp com.mhamauto.cli.CatalogCli stats
```

A fresh import of four real sample rows plus 500 additional synthetic rows may produce 504 distinct product records. The sample file is only a source-format specimen; its source price currency has not been confirmed. The 500 synthetic rows intentionally reuse four source image URLs. The existence of 504 rows does not mean 504 unique product photographs or verified inventory items.

### Step G — Assign fictional test prices only if needed for a demo checkout

```powershell
$cp = 'target/classes;target/dependency/*'
java -cp $cp com.mhamauto.cli.DemoPriceCli seed-synthetic --confirm-fictional-kwd
if ($LASTEXITCODE -ne 0) { throw 'Explicit test price assignment failed' }
java -cp $cp com.mhamauto.cli.CheckoutCli sample-priced
```

This is an **optional academic test-data operation** intended for synthetic products only. It changes local sample-price assignment records. Do **not** use `--source-currency=KWD` on the genuine source sample unless the source currency has been verified independently.

### Step H — Start Tomcat and deploy the WAR

From the same terminal that contains the database environment variables:

```powershell
$tomcat = 'D:\Java Tools\apache-tomcat-10.1.61'
& "$tomcat\bin\shutdown.bat"
# Confirm the old Java/Tomcat process has fully stopped.
Copy-Item -LiteralPath '.\target\mham-auto.war' -Destination "$tomcat\webapps\mham-auto.war" -Force
& "$tomcat\bin\startup.bat"
```

On a *fresh* server, `shutdown.bat` may report that Tomcat is not running; that is acceptable if you confirm no old Tomcat process remains. Wait for the WAR to expand and inspect `logs` on failure. Open `http://localhost:8080/mham-auto/`.

## 16. Build, deployment and test commands

### Recommended final verification sequence

Run from the project root on Windows, with MySQL running and the necessary environment variables set:

```powershell
# 1. Standalone Java/structural regressions
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase11-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Offline gate failed' }

# 2. Actual dependencies + JUnit + WAR + CLI runtime libraries
mvn clean test package dependency:copy-dependencies '-DincludeScope=runtime' '-DoutputDirectory=target/dependency'
if ($LASTEXITCODE -ne 0) { throw 'Maven/JUnit gate failed' }

# 3. Read-only database CLI status checks
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase11-Database.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Database gate failed' }

# 4. After deploying the new WAR and starting Tomcat:
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase11-Live.ps1'
if ($LASTEXITCODE -ne 0) { throw 'HTTP gate failed' }
```

### Re-run a particular phase's tests

| Phase | Offline script | Live script | Database step |
|---|---|---|---|
| 4 | `Test-Phase4-Offline.ps1` | `Test-Phase4.ps1 -SkipBuild` | `IdentityCli status` / V002 |
| 5 | `Test-Phase5-Offline.ps1` | `Test-Phase5.ps1 -SkipBuild` | `CartCli status` / V003 |
| 6 | `Test-Phase6-Offline.ps1` | `Test-Phase6.ps1 -SkipBuild` | `CheckoutCli status` / V004 |
| 7 | `Test-Phase7-Offline.ps1` | `Test-Phase7.ps1 -SkipBuild` | Reuses V004 |
| 8 | `Test-Phase8-Offline.ps1` | `Test-Phase8.ps1 -SkipBuild` | `AdminCli status` / V005 |
| 9 | `Test-Phase9-Offline.ps1` | `Test-Phase9.ps1 -SkipBuild` | `TradeCli status` / V006 |
| 10 | `Test-Phase10-Offline.ps1` | `Test-Phase10.ps1 -SkipBuild` | Read-only performance SQL |
| 11 | `Test-Phase11-Offline.ps1` | `Test-Phase11-Live.ps1` | `Test-Phase11-Database.ps1` |

Example invocation after building and deploying:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase8-Offline.ps1'
if ($LASTEXITCODE -ne 0) { throw 'Phase 8 offline failed' }
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Test-Phase8.ps1' -SkipBuild
if ($LASTEXITCODE -ne 0) { throw 'Phase 8 HTTP failed' }
```

**Test-side effects:** Phase 4, 5, 6 and 7 live scripts can create uniquely named test users; cart, address and checkout tests also change test rows and may create demo orders. Phase 8/9/10/11 live scripts largely check public/anonymous access and do **not** perform full authenticated admin/trade operations. Run the tests against a **backed-up test DB**, not business data. Repeated scripts may leave `.test` accounts and demonstration records in the database.

### Capture evidence logs

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Collect-Phase11-Evidence.ps1' -IncludeDatabase -IncludeHttp
if ($LASTEXITCODE -ne 0) { throw 'Evidence collection failed; inspect evidence/results' }
```

This records offline, Maven, DB CLI and public HTTP logs under `evidence/results`. It does **not** automatically capture authenticated screenshots or fill the manual test matrix. For an HTTP-only or offline-only run, omit the relevant switches.

### Optional performance observation

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File '.\scripts\Measure-Phase10.ps1' -Runs 5
```

Report these as local sample timing observations, not as a capacity guarantee. For Phase 11, run `database/15_phase11_read_only_validation.sql` separately in Workbench and export the results.

## 17. Full manual acceptance journeys

Use **fictional** test data and prepare three separate accounts: Customer A, Customer B and Administrator. If using one browser, sign out fully between roles; separate private sessions make ownership checks easier.

### Customer A — complete purchase simulation

1. Confirm homepage/preview and MySQL catalogue pages load, including SKU search, filters, pagination and image fallback.
2. Register A, log out and log back in. Confirm account and session persist appropriately.
3. Add a synthetic, explicitly priced demo variant to cart; update quantity, refresh, logout/login, remove another item and confirm correct persistence.
4. Create a fictional Kuwait address; validate required fields and maximum/address ownership rules.
5. Review demo checkout. Confirm amounts have three KWD decimal places and no real shipping/tax/payment is implied.
6. Place a demo order, retain its reference, repeat the same checkout submission token and confirm no second order is created.
7. Confirm cart empty; open confirmation and `/orders` history; check SKU/title/quantity/price/address snapshots.
8. Change saved address and/or admin product details later; confirm the historical order snapshot stays unchanged.

### Customer B — isolation checks

1. Register another test account. Verify B cannot see A's cart, address, order details or trade application.
2. Try A's demo order reference in B's `/orders/view?ref=...` and `/order-confirmation?ref=...`; expect owner-scoped denial (e.g., 404).
3. Verify B cannot access `/admin`, `/admin/products`, `/admin/orders`, `/admin/trade-applications` or admin write endpoints (403 for authenticated non-admin).
4. Submit B's own business application and observe its own status only.

### Administrator — product, demo order and B2B workflow

1. Grant ADMIN **locally** to a separately registered account, then log out/in to refresh session role data.
2. Open admin dashboard; verify product/variant/inventory/order counters against sample data.
3. Create category, then an active product with unique handle and SKU; update fictional sample stock and explicit fictional KWD price; verify public product visibility.
4. Attempt duplicate SKU/handle and invalid stock; ensure failure does not leave partial rows.
5. Place a customer demo order, then change the product and review the order as ADMIN; snapshot must remain identical.
6. Approve Customer A's business application and reject Customer B's. Confirm each applicant sees only their decision, private notes remain hidden, and neither acquires wholesale prices/credit/ADMIN roles.
7. Retry the same business review decision; it must not create duplicate review events.
8. Inspect database `admin_events` and `business_application_events` using **redacted** evidence.

### Security and UX cross-cutting checks

- Invalid or missing CSRF token must fail on state-changing forms; forged cross-origin POST must fail.
- Guest protected routes redirect to login; signed-in CUSTOMER admin routes are forbidden.
- Inspect CSP, no-sniff and other response headers; Shopify CDN images still display or fall back locally.
- Test mouse+keyboard, 200% zoom, reduced-motion preference, narrow mobile layout, empty/errors states and accessibility of form labels/focus.
- Make sure browsers are not serving stale cached CSS/JS after Phase 10; hard refresh if needed.

**Acceptance evidence:** use `docs/PHASE_11_TEST_MATRIX.csv` (56 rows). Enter actual `PASS`, `FAIL`, `BLOCKED` or `NOT RUN` outcomes and link to evidence. Do not fill `PASS` for tests you did not execute.

## 18. CSV, images, pricing and scalability

### Shopify CSV data contract

- Input: `samples/product data sample.csv`; **74 Shopify columns**, **four sample product rows**, four source image URLs.
- Source columns cover handles, titles, description HTML, vendor, options/variants, SKU, price, inventory, image position/alt and market flags.
- Multiple CSV rows may represent **one product** (shared handle) with multiple variants/images. The importer must preserve those relationships.
- Two sample SKU strings have a leading apostrophe. Preserve originals; do not convert numeric-looking SKUs to integers automatically.
- Source categories are not reliably structured; some vehicle fitment claims are embedded in description HTML and **not verified**.
- Incorrect repeated alt text, empty categories, unverified stock and source currency are data-quality flags, not grounds to fabricate values.

### Media storage

MySQL uses `media_assets`, `product_images`, `variant_images` and image URL metadata. Images are loaded by the browser from approved Shopify CDN links. **MySQL does not store image BLOBs.** A later production system may migrate licensed media to independently controlled object storage + CDN without changing the logical product/media relationships. Rights and availability must be confirmed before copying externally hosted media.

### Currency and simulated prices

- Kuwait display: `KWD`, three decimal places (fils). Use `DECIMAL(...,3)` / Java `BigDecimal`.
- The sample's raw `Variant Price` values are **not certified KWD**; empty `Price / Kuwait` fields do not establish their currency.
- Unverified source prices stay *pending* in the catalogue/cart and block checkout.
- Only approved, specifically flagged **fictional synthetic-demo** prices may be assigned for academic checkout tests. No live tax or payment is collected.

### Scale target

The schema uses normalized product/variant/media associations, indexed handles/SKUs, bounded listing queries, a connection pool and importer batch behavior as foundations for larger catalogues. The synthetic fixture tests 500 unique product handles/SKUs, but **does not prove 1,000,000+ product throughput**. Benchmarking, search relevance, storage/CDN rights, independent backups, slow-query analysis and concurrency/load testing are required in future development before that claim can be accepted.

## 19. Security and known limitations

### Controls present in the source (still require live testing)

- Salted adaptive password hashing; restricted ADMIN assignment; role/account rechecks for protected routes.
- HttpOnly session cookie, idle timeout, session rotation, POST logout, CSRF tokens and origin-based defense-in-depth.
- Prepared SQL, input bounds, MySQL constraints, transaction and owner checks.
- Escaped JSP output and controlled image domains; CSP, frame/MIME/permissions headers.
- Immutable demo order snapshots, idempotency tokens and basic admin/trade event records.

### Not included or not certified for production

- No real payment gateway/KNET, payment webhooks, financial reconciliation, refunds, fulfilment, returns, stock reservation or real delivery arrangements.
- No validated OEM/VIN fitment engine or licensed compatibility guarantees.
- No wholesale tiers, business credit, RFQ, organization multi-user purchasing or automated business verification.
- No production HTTPS configuration, secure-cookie HTTPS enforcement, admin MFA, disaster recovery tests, monitored audit program or independent penetration test.
- Login throttling is process-local rather than distributed, and the local default JDBC URL disables transport TLS.
- Shopify CDN images and source-data permissions depend on an external account; URL existence does not guarantee continued access or reproduction rights.
- No verified 1M-record benchmark, latency SLO or production capacity certification.

For a formal risk register, read `docs/PHASE_11_SECURITY_AND_LIMITATIONS.md`.

## 20. Academic syllabus, screenshots and report

### Mapping to Java and Web Programming Practical

| Academic competency | Implementation evidence |
|---|---|
| Core Java/OOP | Java model classes, constructors, encapsulation, methods and validation helpers |
| Structured Java programming | Domain packages, service/DAO layers, exceptions/conflicts and transaction logic |
| Frontend and interaction | HTML/CSS, vanilla JavaScript, responsive forms, input feedback, JSON cart/database endpoints |
| Server-side web programming | Servlet GET/POST controllers, JSP/JSTL templates, HttpSession, filters, cookies and Tomcat deployment |
| Database integration | MySQL, JDBC, prepared statements, normalized schema, constraints and migration/import CLIs |
| Extended practical demonstration | Complete no-payment cart/order journey, role-limited ADMIN tools and B2B application workflow |

This follows the original four teaching-phase mapping preserved in `docs/PHASE_11_SYLLABUS_MAPPING.md`. Confirm the current college assessment rubric before submission.

### Screenshot/evidence checklist

Capture **real screenshots from your locally tested application**. The provided checklist proposes 20 files under `evidence/screenshots/`:

1. Homepage desktop and mobile.
2. Database product catalogue and product details.
3. Registration/login and customer account.
4. Cart and Kuwait address form.
5. Demo checkout, confirmation and order history.
6. Admin dashboard, product/category management and demo order review.
7. Business application and admin B2B decision screen.
8. MySQL Workbench schema, Maven/JUnit success, HTTP smoke log and a denied admin-access test.

Use the exact filenames in `docs/PHASE_11_EVIDENCE_CHECKLIST.md`. Mask real contact data and avoid showing passwords. Record errors and fixes rather than editing screenshots to hide failure.

### Academic report and deliverable set

- `docs/MHAM_AUTO_AcademicV1_Project_Report.docx` (editable) and `.pdf` (printable).
- Complete source release ZIP and **a WAR compiled from your own verified local build**.
- All SQL files and the importer sample/fixture, project README, ERD/architecture diagrams, screenshots and completed test matrix.
- Student name/roll number, instructor/institution, project overview, objectives, features, architecture, DB design, testing evidence, known limitations, conclusion and future scope.

The supplied report is a **draft** until owner-specific details, genuine test outcomes and screenshots are completed.

**Integrity note:** replacing the original RC1 root `README.md` changes that file’s SHA-256 digest, so the original `docs/PHASE_11_FILE_SHA256.csv` entry for `README.md` will no longer match your locally documented project. This is expected for a documentation-only replacement; retain the untouched original RC1 ZIP as the authoritative package snapshot and do not claim that the modified project is byte-identical to RC1.

## 21. Troubleshooting and recovery

| Symptom | Likely cause | Action |
|---|---|---|
| `mvn` or `javac` not recognized | Incorrect PATH/JDK | Confirm Java 21 and Maven 3.9.11; restart terminal and use `mvn -version` |
| Maven cannot resolve libraries | Repository/network or proxy issue | Check Maven logs/connectivity and configured repositories; do not edit dependencies blindly |
| `DB 503`, pool error, access denied | Missing credentials, MySQL stopped, wrong host/port or migration | Check local MySQL and `MHAM_DB_*` for both CLI and Tomcat process; do not print passwords |
| Migration checksum mismatch | SQL applied previously differs from file | Compare actual applied version against source and backup; **do not** delete migration rows or rewrite historic migrations |
| Catalogue empty | Import not run, products inactive, Kuwait market flags or connection problem | Run `CatalogCli stats`, check source/import status and MySQL; do not invent sample content |
| Product image broken | Expired/bad CDN URL, CSP or missing remote asset | Verify source URL/access rights and check image fallback; do not store binary in MySQL |
| Price `pending` | Source KWD currency not verified | Use an explicitly fictional synthetic test assignment, or keep checkout blocked |
| HTTP 403 on form | Invalid CSRF, stale session, origin mismatch or insufficient role | Reload form, login again, inspect role and expected Origin; do not disable protections to force success |
| Other customer can access data | Ownership check failure | Treat as a **blocking defect**; stop acceptance and fix before sign-off |
| WAR deploys but old view appears | Stale exploded webapp, cache, old Tomcat process | Stop Tomcat, verify it stopped, redeploy WAR, clear only stale **exploded webapp** if necessary, hard refresh browser |
| JSP compilation error | JSP/taglib mismatch, unsatisfied dependency, missing fragment | Inspect Tomcat logs and `pom.xml`; do not assume offline stub compilation covers JSP |
| Phase 11 offline gate skips Python/Node | Optional tools unavailable | Record skip; run equivalent source/JS checks where possible and do not claim skipped checks passed |
| 500-product test incomplete | Incorrect import, fixture collisions or missing source CSV | Validate fixtures and import reconciliation; distinguish real 4-row input from synthetic 500-row fixture |

**Recovery:** Restore the **source folder** from a timestamped backup and the **database** from a separately verified SQL export. Replacing a WAR does not restore database contents. Do not run fresh-install SQL or destructively clear the database on an existing working project.

## 22. Final release checklist and reference documents

### Final acceptance gates

- [ ] Backups of source and `mham_auto` database created and verified.
- [ ] Java 21 / Maven 3.9.11 / MySQL 8 / Tomcat 10.1.61 verified.
- [ ] Maven compile/JUnit tests pass and `target/mham-auto.war` exists.
- [ ] V001–V006 applied, checksum consistent, schema validation exported.
- [ ] Offline Java, static audit and JavaScript checks executed or skips documented.
- [ ] Tomcat serves homepage, catalogue, health endpoint and authentication pages.
- [ ] Customer A/B identity and ownership tests pass.
- [ ] Cart, Kuwait address, demo checkout, idempotency and order snapshots pass.
- [ ] Admin product/category/inventory/order workflows pass.
- [ ] B2B application submission, review and permission boundaries pass.
- [ ] Phase 10 headers, UX/accessibility and image loading pass.
- [ ] 56-row test matrix completed using observed results and evidence references.
- [ ] Screenshots/logs captured, redacted and accurate.
- [ ] Academic project report and syllabus mapping reviewed and signed off.
- [ ] Known issues either fixed or explicitly accepted for **academic** demonstration.
- [ ] Release decision recorded: **ACCEPTED** only after all mandatory gates pass.

### Authoritative supporting files in RC1

| File | Contents |
|---|---|
| `docs/PHASE_4_GUIDE.md` … `docs/PHASE_10_GUIDE.md` | Individual installation/verification instructions |
| `docs/PHASE_4_MANIFEST.md` … `docs/PHASE_10_MANIFEST.md` | Exact added/updated file history |
| `docs/PHASE_11_RELEASE_GUIDE.md` | Complete source installation and final acceptance |
| `docs/PHASE_11_ARCHITECTURE.md` | Runtime flow, modules and boundaries |
| `docs/PHASE_11_DATABASE.md` | Schema overview, relationships and migrations |
| `docs/PHASE_11_ERD.mmd` | Editable Mermaid entity-relationship diagram |
| `docs/PHASE_11_SCHEMA_DICTIONARY.csv` | Schema/table reference |
| `docs/PHASE_11_TEST_MATRIX.csv` | 56 acceptance scenarios and evidence fields |
| `docs/PHASE_11_EVIDENCE_CHECKLIST.md` | Required local screenshots and logs |
| `docs/PHASE_11_SYLLABUS_MAPPING.md` | Academic Java/Web concepts and code mapping |
| `docs/PHASE_11_VERIFICATION_RESULTS.md` | What was/was not executed during RC1 preparation |
| `docs/PHASE_11_SECURITY_AND_LIMITATIONS.md` | Known risks, exclusions and production restrictions |
| `docs/PHASE_11_QA_AND_SCALE_PLAN.md` | Testing strategy and future scale validation |
| `docs/PHASE_11_SOURCE_PROVENANCE.md` | Source/reconstruction details |
| `docs/PHASE_11_FILE_SHA256.csv` | Integrity manifest for the original RC1 package |
| `docs/PHASE_11_CHANGE_MANIFEST.md` | Phase 11-only additions |

---

## Short project handoff

**The Academic V1 implementation roadmap is delivered through Phase 11, but its final integrated release remains RC1 until your Windows/MySQL/Tomcat tests and the academic evidence matrix pass.** No redesign is required for the current coursework scope. Focus on verification and fixing actual integration defects, then sign off. Preserve the existing Java/JSP/JDBC architecture and avoid turning the simulated retail/B2B workflows into live commercial operations without a separate, reviewed production project.

*README written from the combined RC1 source archive, migration scripts, phase guides/manifests, test scripts and release documents. It describes implemented source features and documented checks; it does not claim unseen local test results.*
