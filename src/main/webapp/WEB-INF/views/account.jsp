<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>My Customer Account | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/customer-portal.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body>
<%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="p7-shell mh-shell">
  <div class="p7-crumb"><a href="${pageContext.request.contextPath}/products">Catalogue</a> / My account</div>
  <div class="p7-heading"><div><p class="p7-eyebrow">ACCOUNT OVERVIEW</p><h1>Welcome back, <c:out value="${sessionScope.authUser.displayName}"/><span>.</span></h1>
    <p>Manage your profile, saved Kuwait addresses and demo orders.</p></div>
    <span class="p7-mode">DEMO ENVIRONMENT · NO LIVE PAYMENTS</span></div>
  <div class="p7-metrics" aria-label="Account overview">
    <div class="p7-metric"><small>DEMO ORDERS</small><strong><c:out value="${dashboard.demoOrders}"/></strong><span>Account only</span></div>
    <div class="p7-metric"><small>SAVED ADDRESSES</small><strong><c:out value="${dashboard.savedAddresses}"/></strong><span>Kuwait address book</span></div>
    <div class="p7-metric"><small>CART ITEMS</small><strong><c:out value="${dashboard.cartUnits}"/></strong><span>Saved quantities</span></div>
    <div class="p7-metric"><small>DEMO TOTAL (UNPAID)</small><strong class="p7-price"><c:out value="${dashboard.demoTotalLabel}"/></strong><span>Never collected or charged</span></div>
  </div>
  <div class="p7-grid"><section class="p7-panel"><div class="p7-sectionhead"><div><p class="p7-eyebrow">YOUR HISTORY</p><h2>Recent demo orders</h2></div><a href="${pageContext.request.contextPath}/orders">All orders</a></div>
    <c:choose><c:when test="${empty dashboard.recent}"><div class="p7-empty"><strong>No demo orders yet</strong><p>Browse the catalogue, add a sample part and complete a demonstration checkout to see it here.</p><a href="${pageContext.request.contextPath}/products">Explore spare parts</a></div></c:when>
      <c:otherwise><div class="p7-orderlist"><c:forEach items="${dashboard.recent}" var="o"><a class="p7-order" href="${pageContext.request.contextPath}/orders/view?ref=<c:out value='${o.reference}'/>"><span class="p7-order-main"><strong><c:out value="${o.reference}"/></strong><small><c:out value="${o.createdAt}"/> · <c:out value="${o.units}"/> units</small></span><span class="p7-order-aside"><b><c:out value="${o.totalLabel}"/></b><small><c:out value="${o.statusLabel}"/></small></span></a></c:forEach></div></c:otherwise></c:choose>
  </section><aside class="p7-panel"><div class="p7-sectionhead"><div><p class="p7-eyebrow">ACCOUNT SETTINGS</p><h2>My profile</h2></div></div>
    <p class="p7-note">Email is your sign-in identifier and cannot be changed in demo environment.</p>
    <c:if test="${profileUpdated}"><p class="p7-success" role="status">Your display name was updated.</p></c:if>
    <c:if test="${not empty profileError}"><p class="p7-error" role="alert"><c:out value="${profileError}"/></p></c:if>
    <form method="post" action="${pageContext.request.contextPath}/account" class="p7-form">
      <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="action" value="updateName">
      <label for="p7-name">Display name</label><input id="p7-name" name="displayName" value="<c:out value='${sessionScope.authUser.displayName}'/>" minlength="2" maxlength="100" autocomplete="name" required>
      <label for="p7-email">Email (read only)</label><input id="p7-email" value="<c:out value='${sessionScope.authUser.email}'/>" readonly>
      <button type="submit">Save profile</button>
    </form>
    <div class="p7-links"><a href="${pageContext.request.contextPath}/trade/status">Trade business application</a><a href="${pageContext.request.contextPath}/addresses">Manage Kuwait addresses</a><a href="${pageContext.request.contextPath}/cart">View shopping cart</a><a href="${pageContext.request.contextPath}/checkout">Demo checkout</a><c:if test="${sessionScope.authUser.admin}"><a href="${pageContext.request.contextPath}/admin">Administrator access</a></c:if></div>
    <form method="post" action="${pageContext.request.contextPath}/logout" class="p7-logout"><input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>"><button type="submit">Securely sign out</button></form>
  </aside></div>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body></html>
