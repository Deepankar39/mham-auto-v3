<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Demo Checkout | MHAM AUTO Kuwait</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body>
<%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="c6-shell mh-shell">
  <div class="c6-crumb"><a href="${pageContext.request.contextPath}/cart">Cart</a> / Demo checkout</div>
  <p class="c6-kicker">06 / DEMO ENVIRONMENTNSTRATION</p>
  <h1>Review demo order<span>.</span></h1>
  <p class="c6-lede">Your total is calculated again on the server when you place the demonstration order.</p>
  <div class="c6-demo-banner" role="note"><strong>DEMO — NO PAYMENT OR DELIVERY.</strong> This places only a database record. It does not charge money, reserve inventory, create a real order or dispatch products.</div>
  <div class="c6-columns">
    <section class="c6-panel"><h2>1. Your saved address</h2>
      <c:if test="${empty addressList}"><div class="c6-alert">You need a Kuwait address before demo checkout. <a href="${pageContext.request.contextPath}/addresses">Add an address</a>.</div></c:if>
      <c:if test="${not empty addressList}"><p class="c6-muted">Choose an address. The recipient details will be copied into your order record.</p></c:if>
      <form method="post" action="${pageContext.request.contextPath}/checkout" class="c6-form" id="demo-checkout-form">
        <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
        <input type="hidden" name="checkoutToken" value="<c:out value='${checkoutToken}'/>">
        <fieldset class="c6-address-list" <c:if test="${empty addressList}">disabled</c:if>><legend class="c6-visually-hidden">Select demo delivery address</legend>
          <c:forEach items="${addressList}" var="a" varStatus="i">
            <label class="c6-radio"><input type="radio" name="addressId" value="<c:out value='${a.id}'/>" <c:if test="${i.first}">checked</c:if> required>
              <span><strong><c:out value="${a.recipientName}"/></strong><small><c:out value="${a.phone}"/> · <c:out value="${a.governorateLabel}"/></small><small><c:out value="${a.summary}"/></small></span>
            </label>
          </c:forEach>
        </fieldset>
        <a class="c6-text-link" href="${pageContext.request.contextPath}/addresses">Manage your Kuwait addresses</a>
        <h2>2. Confirm the demo</h2>
        <p class="c6-muted">Prices are KWD, where verified or explicitly assigned as fictional demo data. Sample stock counts are not confirmed real inventory.</p>
        <c:choose><c:when test="${not canCheckout}"><div class="c6-alert">Checkout is blocked until your cart has items with eligible, verified/explicit demo KWD prices and available sample quantities. <a href="${pageContext.request.contextPath}/cart">Review cart</a>.</div></c:when>
          <c:when test="${empty addressList}"><button type="button" class="c6-primary" disabled>Add an address before placing demo order</button></c:when>
          <c:otherwise><button class="c6-primary" type="submit">Place DEMO order — no payment</button></c:otherwise></c:choose>
      </form>
    </section>
    <aside class="c6-panel c6-summary"><h2>Order summary</h2>
      <c:choose><c:when test="${empty cart.items}"><p>Your cart is empty. <a href="${pageContext.request.contextPath}/products">Browse parts</a>.</p></c:when>
      <c:otherwise><c:forEach var="line" items="${cart.items}"><div class="c6-line"><div><strong><c:out value="${line.title}"/></strong><small>SKU <c:out value="${line.sku}"/> · Qty <c:out value="${line.quantity}"/></small></div><span><c:out value="${line.lineTotalLabel}"/></span></div></c:forEach></c:otherwise></c:choose>
      <div class="c6-totals"><div><span>Cart subtotal</span><strong><c:out value="${cart.subtotalLabel}"/></strong></div>
        <div><span>Shipping</span><span>KWD 0.000 (not charged)</span></div><div><span>Tax</span><span>KWD 0.000 (not charged)</span></div>
        <div class="c6-total"><span>Demo total</span><strong><c:out value="${cart.subtotalLabel}"/></strong></div></div>
      <p class="c6-small">A demo total is an educational snapshot, not a payable invoice. No real checkout or delivery.</p>
    </aside>
  </div>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body></html>
