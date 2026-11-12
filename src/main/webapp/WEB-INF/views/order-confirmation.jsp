<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Demo Order Confirmation | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body><%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main class="c6-shell mh-shell" id="main-content">
  <div class="c6-crumb"><a href="${pageContext.request.contextPath}/products">Catalogue</a> / Demo confirmation</div>
  <div class="c6-confirm"><div class="c6-symbol" aria-hidden="true">✓</div><p class="c6-kicker">DEMONSTRATION ORDER SAVED</p>
    <h1>Demo order recorded<span>.</span></h1>
    <p>Reference <strong><c:out value="${order.reference}"/></strong></p>
    <p>Recorded <c:out value="${order.createdAt}"/> (server time)</p>
    <div class="c6-demo-banner"><strong>NO REAL PURCHASE OCCURRED.</strong> No money was collected, no inventory reserved and no shipping scheduled.</div>
  </div>
  <div class="c6-columns"><section class="c6-panel"><h2>Item snapshots</h2>
    <c:forEach items="${order.lines}" var="line"><div class="c6-line"><div><strong><c:out value="${line.title}"/></strong>
      <small>SKU <c:out value="${line.sku}"/> · Qty <c:out value="${line.quantity}"/></small><small>Unit <c:out value="${line.unitPriceLabel}"/></small><c:if test="${line.fictionalPrice}"><small><strong>Fictional demo price</strong></small></c:if></div><strong><c:out value="${line.lineTotalLabel}"/></strong></div></c:forEach>
    <div class="c6-totals"><div><span>Subtotal</span><strong><c:out value="${order.subtotalLabel}"/></strong></div>
      <div><span>Shipping and tax (demo)</span><span>KWD 0.000</span></div>
      <div class="c6-total"><span>Snapshot total</span><strong><c:out value="${order.totalLabel}"/></strong></div></div>
  </section><aside class="c6-panel"><h2>Saved address snapshot</h2>
    <strong><c:out value="${order.recipientName}"/></strong><p><c:out value="${order.phone}"/></p>
    <p><c:out value="${order.governorateLabel}"/></p><p><c:out value="${order.addressLabel}"/></p>
    <c:if test="${not empty order.floorDetail}"><p><c:out value="${order.floorDetail}"/></p></c:if>
    <c:if test="${not empty order.directions}"><p><c:out value="${order.directions}"/></p></c:if>
    <p class="c6-muted">Status: <c:out value="${order.status}"/></p>
    <a class="c6-secondary" href="${pageContext.request.contextPath}/products">Return to catalogue</a>
    <p class="c6-small">You can access every saved demo order in <a href="${pageContext.request.contextPath}/orders">My orders</a>.</p>
  </aside></div>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %></body></html>
