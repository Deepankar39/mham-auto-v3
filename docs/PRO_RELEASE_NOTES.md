# MHAM AUTO — Pro Experience Engineering Release

## Scope and baseline

Redesign operates as a non-destructive copy of the previously delivered Java 21/Tomcat/JSP/JDBC/MySQL project. The original uploaded archive remains unchanged. Source import conventions, session protections, CSRF, database migrations, original ordering and trade-review logic were not replaced. No new SQL migration is introduced.

## Route additions

| Method | Route | Capability | Authorization |
|---|---|---|---|
| GET | `/featured` | Render escaped 4-card product highlight HTML | Public |
| GET | `/wishlist` | Local-only wishlist page | Public |
| GET | `/admin/customers` | Read-only customer table with prefix filter/page | ADMIN |
| POST | `/admin/product/archive` | Archive/unpublish a product | ADMIN + CSRF |
| POST | `/admin/categories`, `action=rename` | Rename admin-created category | ADMIN + CSRF |
| POST | `/admin/categories`, `action=delete` | Delete empty admin-created category | ADMIN + CSRF |

Existing routes and form parameters are unchanged, apart from the new optional `status` query parameter on `/admin/products` and a required `action=create` hidden field on new category creation.

## Backend risk decisions

- `AdminDao.products()` preserves the original two-argument method and adds a validated three-argument overload. Search now covers handle, title, SKU prefix; optional status from a fixed whitelist.
- Archive updates `products.status='archived'` and `published=FALSE`; no hard deletion of products/order references.
- Category mutating actions use a transaction and lock, require taxonomy prefix `ACADEMIC_ADMIN:` and log events. Imported taxonomy cannot be renamed or deleted through this route. Deletion also checks product assignment; database FKs remain enforced.
- Customer directory uses prepared statements, bounded search, 20-row pages, no password hash, no arbitrary PII expansion, ADMIN route protection and normal HTTP no-store policy.
- `/featured` reuses `CatalogDao.search`, not dummy product data. A database outage degrades gracefully to a browse link on the homepage.
- Local shortlist stores title/URL/image in `localStorage`; URLs and image hosts are validated before rendering. All inserted text uses DOM `textContent`; no user-controlled HTML is injected.
- CSP, protected-route filter, CSRF, login throttle and authentication backend are not relaxed.

## Design system

- MHAM uploaded image cropped with preserved alpha and resized without new artwork/branding invention.
- Yellow `#F7BD3E`, charcoal `#1E2022`, warm grey and white; local sans-serif stack; no external font network calls.
- Dedicated admin sidebar, keyboard-accessible mobile toggle, product cards, product gallery thumbnails, forms, tables and placeholders.
- Mobile breakpoints at ~1200, 970, 760 and 480 px, with reduced-motion support.

## Remaining live acceptance

This environment lacked Maven, MySQL, Tomcat and usable browser navigation. Source-level validation and compile checks do **not** prove server behaviour or end-to-end integration. On Windows, run `mvn clean test package`, deploy under Tomcat 10.1, then execute `docs/PRO_ACCEPTANCE_CHECKLIST.md` with a test database. Independently check permissions, SQL queries, browser layout at 390/768/1440 px, all admin form paths and demo-order state transitions before client/portfolio demonstration.
