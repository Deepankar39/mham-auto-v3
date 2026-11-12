<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Trade Application Status | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/trade.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body><%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="t9-shell mh-shell"><a class="t9-back" href="${pageContext.request.contextPath}/account">← My account</a>
<div class="t9-hero"><p class="t9-eyebrow">TRADE APPLICATION</p><h1>Application status</h1><p>Track the simple admin review for your MHAM AUTO business request.</p></div>
<section class="t9-card t9-status"><div class="t9-status-top"><div><span class="t9-caption">APPLICATION #<c:out value="${application.id}"/></span><h2><c:out value="${application.companyName}"/></h2></div><span class="t9-pill t9-${application.status}"><c:out value="${application.status}"/></span></div>
<div class="t9-details"><div><small>Business type</small><strong><c:out value="${application.businessType}"/></strong></div><div><small>Governorate</small><strong><c:out value="${application.governorate}"/></strong></div><div><small>Submitted</small><strong><c:out value="${application.createdAt}"/></strong></div><div><small>Contact</small><strong><c:out value="${application.contactName}"/></strong></div></div>
<c:choose><c:when test="${application.status eq 'PENDING'}"><p class="t9-note">Your application is pending demonstration admin review.</p></c:when>
<c:when test="${application.status eq 'APPROVED'}"><p class="t9-note">Approved in the demo review only. No trade privileges, discounts or credit were activated.</p></c:when>
<c:otherwise><p class="t9-note">This application was rejected in the demo review. Contact the demonstration administrator if a correction is required.</p></c:otherwise></c:choose>
<div class="t9-actions"><a href="${pageContext.request.contextPath}/products">Browse retail catalogue</a><a href="${pageContext.request.contextPath}/account">Back to account</a></div>
</section><p class="t9-disclaimer">DEMO ONLY · No financial or wholesale entitlement is granted by application or approval.</p>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %></body></html>
