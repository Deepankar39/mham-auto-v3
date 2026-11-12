<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Sign In | MHAM AUTO</title>
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
<%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="a4-main">

<section class="a4-panel" aria-labelledby="a4-page-title">
  <p class="a4-eyebrow">SECURE CUSTOMER ACCESS</p>
  <h1 id="a4-page-title">Welcome back<span>.</span></h1>
  <p class="a4-intro">Sign in to access your account. This is a demonstration store—ordering and payments are not active.</p>
  <c:if test="${not empty error}"><div class="a4-error" role="alert"><c:out value="${error}"/></div></c:if>
  <form action="${pageContext.request.contextPath}/login" method="post" class="a4-form">
    <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
    <label for="a4-email">Email address</label>
    <input id="a4-email" type="email" name="email" maxlength="254" autocomplete="username" required value="<c:out value='${param.email}'/>" placeholder="you@example.com">
    <label for="a4-password">Password</label>
    <div class="a4-password"><input id="a4-password" type="password" name="password" autocomplete="current-password" required maxlength="128"><button type="button" class="a4-show" data-toggle-password="a4-password" aria-label="Show password">Show</button></div>
    <button class="a4-submit" type="submit">Sign in <svg class="mh-icon" aria-hidden="true" focusable="false"><use href="#mh-icon-arrow-right"/></svg></button>
  </form>
  <p class="a4-switch">New to MHAM AUTO? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
</section>

</main>
<%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body>
</html>
