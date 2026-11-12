<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>My Demo Orders | MHAM AUTO</title>
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
  <div class="p7-crumb"><a href="${pageContext.request.contextPath}/account">Account</a> / Demo order history</div>
  <div class="p7-heading"><div><p class="p7-eyebrow">07 / ORDER HISTORY</p><h1>Your demonstration orders<span>.</span></h1><p>Order history is private to your account. All values are fictional demonstration records, not purchases.</p></div><span class="p7-mode">DEMO ONLY</span></div>
  <div class="p7-notice"><strong>No payments or deliveries.</strong> Orders here are demo snapshots. Inventory has not been reserved.</div>
  <section class="p7-panel"><div class="p7-sectionhead"><h2>Recorded orders (<c:out value="${ordersPage.total}"/>)</h2><a href="${pageContext.request.contextPath}/products">Continue browsing</a></div>
    <c:choose><c:when test="${empty ordersPage.orders}"><div class="p7-empty"><strong>No demo orders yet.</strong><p>After completing a demo checkout your order will appear here.</p><a href="${pageContext.request.contextPath}/cart">Open cart</a></div></c:when>
    <c:otherwise><div class="p7-orderlist"><c:forEach items="${ordersPage.orders}" var="o">
      <a class="p7-order" href="${pageContext.request.contextPath}/orders/view?ref=<c:out value='${o.reference}'/>"><span class="p7-order-main"><strong><c:out value="${o.reference}"/></strong><small>Placed: <c:out value="${o.createdAt}"/> · <c:out value="${o.lines}"/> lines / <c:out value="${o.units}"/> units</small></span><span class="p7-order-aside"><b><c:out value="${o.totalLabel}"/></b><small><c:out value="${o.statusLabel}"/></small></span></a>
    </c:forEach></div></c:otherwise></c:choose>
    <nav class="p7-pagination" aria-label="Order history pages"><c:if test="${ordersPage.hasPrevious}"><a href="${pageContext.request.contextPath}/orders?page=<c:out value='${ordersPage.previous}'/>">← Previous</a></c:if><span>Page <c:out value="${ordersPage.number}"/> of <c:out value="${ordersPage.pages}"/></span><c:if test="${ordersPage.hasNext}"><a href="${pageContext.request.contextPath}/orders?page=<c:out value='${ordersPage.next}'/>">Next</a></c:if></nav>
  </section>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %></body></html>
