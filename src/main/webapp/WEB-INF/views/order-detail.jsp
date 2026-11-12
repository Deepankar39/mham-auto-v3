<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Demo Order Detail | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer-portal.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body><%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="p7-shell mh-shell">
  <div class="p7-crumb"><a href="${pageContext.request.contextPath}/account">Account</a> / <a href="${pageContext.request.contextPath}/orders">Order history</a> / Order detail</div>
  <div class="p7-heading"><div><p class="p7-eyebrow">07 / STORED DEMO ORDER</p><h1>Order <c:out value="${order.reference}"/><span>.</span></h1><p>Recorded <c:out value="${order.createdAt}"/> (server time) · <c:out value="${order.status}"/></p></div><span class="p7-mode">DEMO · UNPAID</span></div>
  <div class="p7-notice"><strong>Not an actual purchase.</strong> No money was collected, no stock reserved, and no delivery arranged. Historical product, price and delivery-address snapshots are shown below.</div>
  <div class="p7-grid"><section class="p7-panel"><div class="p7-sectionhead"><h2>Product snapshots</h2><a href="${pageContext.request.contextPath}/orders">All orders</a></div>
    <c:forEach items="${order.lines}" var="line"><div class="p7-order p7-line"><div class="p7-order-main"><strong><c:out value="${line.title}"/></strong><small>SKU <c:out value="${line.sku}"/> · Quantity <c:out value="${line.quantity}"/></small><small>Unit price <c:out value="${line.unitPriceLabel}"/></small><c:if test="${line.fictionalPrice}"><small class="p7-fake">Fictional demo price</small></c:if></div><div class="p7-order-aside"><b><c:out value="${line.lineTotalLabel}"/></b></div></div></c:forEach>
    <div class="p7-total"><span>Subtotal</span><strong><c:out value="${order.subtotalLabel}"/></strong></div><div class="p7-total"><span>Shipping and tax (demo)</span><strong>KWD 0.000</strong></div><div class="p7-total p7-grand"><span>Demo total (unpaid)</span><strong><c:out value="${order.totalLabel}"/></strong></div>
  </section><aside class="p7-panel"><div class="p7-sectionhead"><h2>Delivery address snapshot</h2></div>
    <p><strong><c:out value="${order.recipientName}"/></strong></p><p><c:out value="${order.phone}"/></p><p><c:out value="${order.governorateLabel}"/></p><p><c:out value="${order.addressLabel}"/></p>
    <c:if test="${not empty order.floorDetail}"><p><c:out value="${order.floorDetail}"/></p></c:if><c:if test="${not empty order.directions}"><p><c:out value="${order.directions}"/></p></c:if>
    <p class="p7-note">Editing or deleting a saved address does not change this historic order snapshot.</p><a class="p7-linkbtn" href="${pageContext.request.contextPath}/orders">← Return to order history</a>
  </aside></div>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %></body></html>
