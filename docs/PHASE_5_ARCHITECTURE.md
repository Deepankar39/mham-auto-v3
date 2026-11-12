# Phase 5 Architectural Note / Shopping Cart

**User** (V002) 1—0..1 **Cart** (V003), **Cart** 1—many **CartItem**, **CartItem** many—1 **ProductVariant** (V001). Unique `carts.user_id` and `(cart_id,variant_id)` guarantee one cart per user and one line per variant.

No image BLOBs or redundant imported price data are stored in the cart. Product image URLs are read through `product_images` + `media_assets` and constrained by existing `ExternalImageUrlPolicy`. All cart prices are retrieved from `product_variants.retail_price_kwd` using `BigDecimal` (KWD with 3 fractional digits). If an imported price remains null because CSV source currency is unknown, the cart retains the selection but hides the complete subtotal.

Mutation path: `ProtectedPagesFilter` (DB role/status refresh) → `CartServlet` (CSRF + parameter validation) → `CartService` (transaction) → `CartDao` (prepared SQL). Serialized writes lock the parent cart row. Deactivating a product or disabling Kuwait eligibility prevents new adds/updates; users may still remove inaccessible cart lines. The source inventory quantity is a **validation ceiling**, not verified sellable inventory, and add-to-cart does not reserve stock.

Limits for Academic V1: 100 distinct SKU variants per cart, 20 units per line. Add to existing variant increments quantity only within those bounds. Atomic rollback on any failure. POST/Redirect/GET prevents accidental repeat submissions on refresh. Sessions never hold full cart state. Navigation dynamically fetches `/api/cart/count` via same-origin authenticated cookies and no-store caching.

**Phase 6 dependency:** server-side demo checkout must revalidate login, cart ownership, visibility, price verification, sample inventory, demo address and current totals inside a transaction, and snapshot the product title/SKU/unit price at checkout. Phase 5 does not create orders or reserve stock.
