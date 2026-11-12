# Phase 6 — checkout architecture

## Relational model

- `users` (V002) 1:N `customer_addresses` (V004): owner scope on every lookup and mutation.
- `users` 1:N `demo_orders`: immutable address snapshots and computed KWD totals.
- `demo_orders` 1:N `demo_order_items`: title/SKU/unit-price/quantity/line-total and `price_basis` provenance snapshots.
- `product_variants` is referenced as a nullable convenience ID; order snapshot remains intact if a variant is deleted.
- `demo_price_assignments`: explicit marker for optional synthetic-only fictional KWD price assignment; original Shopify price strings retained in V001.

## Main transaction

1. CSRF and authenticated user checks; validate server-issued checkout token and chosen owner address.
2. Start JDBC transaction; lock customer cart row (`CartDao.lockedCart`) to serialize other mutations.
3. Check for existing order under unique `(user_id, checkout_token)`; return its reference on a replay.
4. Read owner address; lock current cart item/catalogue rows (`FOR UPDATE`); re-check active/published/Kuwait eligibility, quantities and three-decimal KWD prices.
5. Server computes BigDecimal line totals and subtotal. Demo shipping=tax=0.000 and total=subtotal.
6. Insert immutable demo order + immutable item snapshots; delete cart lines; commit as a single unit.
7. Redirect POST to owner-scoped confirmation GET (PRG). All other errors roll back and do not clear cart.

No stock is reserved or decremented, and no payment processing is performed. No raw CSV HTML is emitted into the checkout views. JSP emits values through `c:out` to avoid XSS. No client field controls order total, role or currency.

## Concurrency & constraints

- Unique user+token, foreign keys, money/quantity checks, indexed owner queries.
- Cart row lock serializes two checkouts or cart changes belonging to one user.
- Replaying a successful token returns the same order even when the browser has rotated to a new session nonce.
- Existing `schema_migrations` tracks SHA-256 of V004 SQL file; preserves V001/V002/V003 checksums.

## Large catalogue compatibility

Orders store stable variant IDs but do not render via live joins; confirmation is from immutable snapshots and scales independently of product catalogue size. Checkout queries at most 100 current cart lines rather than scanning the million-product catalogue.
