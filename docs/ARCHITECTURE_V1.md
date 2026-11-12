# MHAM AUTO V1 — Architecture baseline after Phase 1.4

Browser -> Tomcat -> Servlet/filter -> Service -> (future DAO/JDBC -> MySQL) -> JSP -> Browser.

## Implemented now
- `com.mhamauto.servlet`: catalogue preview and basic liveness Servlet.
- `com.mhamauto.model` and `service`: existing read-only preview objects and logic.
- `com.mhamauto.config`: immutable application settings and startup listener.
- `com.mhamauto.filter`: HTTP request/response defaults; **not** an auth filter.
- `com.mhamauto.web`: server-only JSP view paths.
- `com.mhamauto.media`: external CDN URL validation policy, no network fetch.
- `WEB-INF/views/fragments`: common document meta + page-family header/footer templates.
- `WEB-INF/views/error.jsp`: generic server-side 404/500 fallback.

## Next phases, not yet implemented
- Phase 2: MySQL datasource / JDBC, DAOs, CSV validation/grouping/import, product/variant/image metadata and import audit.
- Phase 3: DB-backed browse/search/detail and HTML-escaped dynamic output.
- Phase 4 onward: sessions, auth/roles, cart, demo orders, admin, basic B2B requests.

## Rules
- No SQL/credentials in JSP or Java source.
- Do not trust the raw `Image Src` as HTML or download it without a controlled allowlist.
- `cdn.shopify.com` is the current approved *structural URL pattern*, not proof of media rights or reachability.
- Store original source CSV identifiers and raw references; apply documented normalization before using unique keys.
- Store URLs/media IDs in MySQL; never image BLOBs. Source currency still needs owner verification.
- Do not mark products compatible with vehicles based only on unverified free-form HTML descriptions.
- Academic configuration enforces demo mode; no live checkout or stock commitment.
