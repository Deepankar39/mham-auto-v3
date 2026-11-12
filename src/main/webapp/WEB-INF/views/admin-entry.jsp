<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Admin Access | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/admin-shell-start.jspf" %>
<main id="main-content" class="a4-main">

<section class="a4-panel a4-dashboard" aria-labelledby="a4-page-title">
  <p class="a4-eyebrow">ADMIN / ROLE CHECK</p>
  <h1 id="a4-page-title">Administrator <span>access.</span></h1>
  <p class="a4-intro">You have passed the server-side ADMIN role check, <c:out value="${sessionScope.authUser.displayName}"/>. Your administrator workspace is available below.</p>
  <div class="a4-user-card"><div><small>ROLE</small><strong>ADMIN</strong></div><div><small>STATUS</small><strong>Authorized session</strong></div></div>
  <div class="a4-actions"><a href="${pageContext.request.contextPath}/account">Customer account</a><a href="${pageContext.request.contextPath}/admin">Go to management dashboard</a></div>
  <form method="post" action="${pageContext.request.contextPath}/logout" class="a4-logout"><input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>"><button type="submit">Securely sign out</button></form>
</section>

</main>
<%@ include file="/WEB-INF/views/fragments/admin-shell-end.jspf" %>
</body>
</html>
