<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Your Shopping Cart | MHAM AUTO Kuwait</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/products.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cart.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body>
<%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="c5-main mh-shell">
  <p class="p3-kicker">05 / CUSTOMER CART</p><h1>Your saved parts<span>.</span></h1>
  <p class="c5-lead">Your parts stay saved to your account when you sign in again.</p>
  <div class="c5-warning" role="note"><strong>DEMO ENVIRONMENT:</strong> No payment, delivery, or inventory reservation is performed. Imported prices and stock may be unverified. Demo checkout requires confirmed or explicitly fictional demo KWD prices.</div>
  <c:if test="${not empty notice}"><div class="c5-notice" role="status"><c:out value="${notice}"/></div></c:if>
  <c:choose>
    <c:when test="${not cart.hasItems}">
      <section class="c5-empty"><h2>Your cart is empty</h2><p>Explore parts and save items for your demo order.</p><a class="c5-btn" href="${pageContext.request.contextPath}/products">Explore product catalogue</a></section>
    </c:when>
    <c:otherwise>
      <div class="c5-layout"><section class="c5-items" aria-label="Items in cart">
        <h2>Saved items (<c:out value="${cart.distinctLines}"/>)</h2>
        <c:forEach var="item" items="${cart.items}">
          <article class="c5-item">
            <div class="c5-thumbnail" data-fallback="${pageContext.request.contextPath}/assets/images/parts-placeholder.svg">
              <c:choose>
                <c:when test="${not empty item.imageUrl}"><img loading="lazy" width="112" height="112" src="<c:out value='${item.imageUrl}'/>" alt="<c:out value='${item.title}'/>"></c:when>
                <c:otherwise><img width="112" height="112" src="${pageContext.request.contextPath}/assets/images/parts-placeholder.svg" alt="Product image unavailable"></c:otherwise>
              </c:choose>
            </div>
            <div class="c5-info"><h3><a href="${pageContext.request.contextPath}<c:out value='${item.productUrl}'/>"><c:out value="${item.title}"/></a></h3>
              <p class="c5-sku">SKU: <c:out value="${item.sku}"/></p><p class="c5-stock"><c:out value="${item.stockLabel}"/></p>
              <c:if test="${not item.available}"><strong class="c5-alert">Review or remove this unavailable item</strong></c:if>
              <p class="c5-price"><c:out value="${item.priceLabel}"/> per unit</p>
              <form method="post" action="${pageContext.request.contextPath}/cart" class="c5-line-form">
                <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="variantId" value="<c:out value='${item.variantId}'/>">
                <label>Qty <input type="number" name="quantity" value="<c:out value='${item.quantity}'/>" min="1" max="20" required></label>
                <button type="submit" <c:if test="${not item.canUpdate}">disabled</c:if>>Update</button>
              </form>
              <form method="post" action="${pageContext.request.contextPath}/cart" class="c5-remove-form">
                <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
                <input type="hidden" name="action" value="remove">
                <input type="hidden" name="variantId" value="<c:out value='${item.variantId}'/>">
                <button type="submit">Remove item</button>
              </form>
            </div><div class="c5-item-total"><small>Line total</small><strong><c:out value="${item.lineTotalLabel}"/></strong></div>
          </article>
        </c:forEach>
        <form method="post" action="${pageContext.request.contextPath}/cart" class="c5-clear">
          <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="action" value="clear">
          <button type="submit" data-confirm-clear="true">Clear entire cart</button>
        </form>
      </section>
      <aside class="c5-summary" aria-label="Cart summary"><h2>Cart summary</h2>
        <div class="c5-summary-row"><span>Distinct parts</span><strong><c:out value="${cart.distinctLines}"/></strong></div>
        <div class="c5-summary-row"><span>Total units</span><strong><c:out value="${cart.totalQuantity}"/></strong></div>
        <div class="c5-summary-row c5-grand"><span>Estimated subtotal</span><strong><c:out value="${cart.subtotalLabel}"/></strong></div>
        <c:if test="${cart.pendingPrices}"><p class="c5-alert">One or more prices await KWD verification. No complete subtotal is available.</p></c:if>
        <c:if test="${cart.unavailable}"><p class="c5-alert">One or more items require availability review.</p></c:if>
        <p>Demo shipping and tax are KWD 0.000; no live fees, payment or delivery occurs.</p>
        <c:choose>
          <c:when test="${cart.hasCompleteSubtotal}"><a class="c5-btn" href="${pageContext.request.contextPath}/checkout">Review DEMO checkout</a></c:when>
          <c:otherwise><a class="c5-continue" href="${pageContext.request.contextPath}/checkout">Review checkout requirements</a></c:otherwise>
        </c:choose>
        <a class="c5-continue" href="${pageContext.request.contextPath}/products">← Continue shopping</a>
      </aside></div>
    </c:otherwise>
  </c:choose>
</main>
<%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
<script defer src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
</body></html>
