# MCA Semester I — Java & Web Programming Practical mapping

MHAM AUTO | Academic V1 | Phase 11 | Release candidate RC1 | 28 October 2026


Mapping follows the original course/proposal's four teaching phases. Exact college grading outcomes should be verified against the current official practical brief.

| Academic phase | Competency / demo | Source evidence | Live demonstration |
|---|---|---|---|
| 1 — Core Application Setup and Design | Java classes, constructors, encapsulation, models, validation | `model/CatalogPreviewCategory.java`, `catalog/CatalogModels.java`, `cart/CartModels.java`, `checkout/CheckoutModels.java` | Explain a product/variant class and validation path |
| 2 — Structured Programming and Reliability | Packages, layered components, exceptions/interfaces, safe error behavior | `service/CatalogPreviewService.java`, `cart/CartService.java`, `checkout/CheckoutConflict.java`, `dao/CatalogImportDao.java` | Show Service -> DAO flow and invalid input handling |
| 3 — Web Interface and Client Interaction | HTML, CSS, JS, responsive forms, navigation, validation, JSON response | `src/main/webapp/index.jsp`, `assets/css/`, `assets/js/`, `servlet/CartCountServlet.java`, `servlet/DatabaseStatusServlet.java` | Home, catalogue search, cart badges, mobile menu |
| 4 — Server-Side Integration and Execution | Servlets GET/POST, JSP, sessions, login/logout, role protection, JDBC/MySQL, Tomcat deployment | `servlet/IdentityServlet.java`, `auth/SessionAuth.java`, `filter/ProtectedPagesFilter.java`, `db/DbPool.java`, `pom.xml` | Register/login/logout; MySQL-backed product; unauthorized admin rejection |

### Enhanced Academic V1 extension
- Phase 5: persistent cart and price checks (`cart/`).
- Phase 6: Kuwait saved address and DEMO order transaction (`checkout/`).
- Phase 7: account dashboard and customer order history (`account/`).
- Phase 8: ADMIN CRUD, sample inventory and demo order state transitions (`admin/`).
- Phase 9: basic trade application/approval (`trade/`); no contracted wholesale benefits.
- Phase 10: user experience, response headers, same-origin checks and static asset optimization (`filter/`, `web/`, `assets/`).

### Suggested short viva questions
1. Why use a Servlet controller, Service and DAO rather than SQL inside JSP?
2. How is an HttpSession created and invalidated, and how is an ADMIN role verified?
3. Why do we use a prepared statement for SQL and `BigDecimal` for KWD?
4. Why separate products, variants and media assets in MySQL?
5. How do transactions and checkout tokens prevent incomplete/doubled demo orders?
6. Why does B2B application approval not grant wholesale privileges?
7. Which parts are only academic demonstrations and what changes before production?

### Evidence to include in the college file
Java class/package screenshot, running Tomcat URL, login session, MySQL schema, product search, cart, demo order history, ADMIN view, B2B application, test results, ERD and architecture flow.
