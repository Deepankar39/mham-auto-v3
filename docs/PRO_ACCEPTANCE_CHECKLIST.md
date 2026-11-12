# MHAM AUTO — Live Acceptance Matrix

Record actual results after deploying to Tomcat 10.1 against a protected test copy of your MySQL database. Do not mark an untested item as PASS.

| Area | Test | Status | Evidence |
|---|---|---|---|
| Build | `mvn clean test package` with Java 21 | NOT RUN HERE | |
| Deployment | WAR loads with no Jasper/JSP compile errors | NOT RUN HERE | |
| Homepage | Logo, hero search, category links, dynamic featured products | NOT RUN HERE | |
| Homepage | Database unavailable leaves homepage usable | NOT RUN HERE | |
| Product listing | Search, vendor/category, sorting, pagination, no results | NOT RUN HERE | |
| Product details | Images, gallery thumbnails, variants, add to cart | NOT RUN HERE | |
| Saved parts | Heart toggle, saved list, remove, browser-local disclosure | NOT RUN HERE | |
| Account | Register, login/logout, profile, saved addresses | NOT RUN HERE | |
| Cart | Add/update/remove/clear, KWD subtotal, session persistence | NOT RUN HERE | |
| Demo checkout | Address selection, valid demo order, order history | NOT RUN HERE | |
| Admin access | Customer user gets 403 for every `/admin/*` route | NOT RUN HERE | |
| Admin dashboard | All DB metrics and recent demo orders correct | NOT RUN HERE | |
| Admin products | Search by name/handle/SKU, status, edit, image, pagination | NOT RUN HERE | |
| Admin archive | CSRF protected, archives, public catalogue hides record | NOT RUN HERE | |
| Categories | Create, rename own category, delete empty own category | NOT RUN HERE | |
| Categories | Imported categories cannot be renamed or deleted | NOT RUN HERE | |
| Customers | Search/pagination/role filter; never expose passwords | NOT RUN HERE | |
| Orders | Existing demo state transitions and immutable snapshots | NOT RUN HERE | |
| Trade | Submit/review application and status display | NOT RUN HERE | |
| Browser | Desktop 1440, tablet 768, mobile 390 px; no horizontal overflow | NOT RUN HERE | |
| Accessibility | Keyboard-only tab order, focus states, reduced motion | NOT RUN HERE | |
| Security | CSRF failure, same-origin check, XSS escape and CSP | NOT RUN HERE | |
| SQL integrity | Run read-only V006/Phase 11 checks on test DB | NOT RUN HERE | |

**Status vocabulary:** PASS / FAIL / NOT RUN / BLOCKED. Include screenshots, test date, browser, JDK, Maven, MySQL and Tomcat versions for real acceptance.
