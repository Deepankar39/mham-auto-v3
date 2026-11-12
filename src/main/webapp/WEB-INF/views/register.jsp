<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Create Account | MHAM AUTO</title>
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
  <p class="a4-eyebrow">04 / NEW CUSTOMER ACCOUNT</p>
  <h1 id="a4-page-title">Create your <span>account.</span></h1>
  <p class="a4-intro">Register as a retail customer. Trade customers can apply for business review after registration. Registering does not grant administrator privileges.</p>
  <c:if test="${not empty error}"><div class="a4-error" role="alert"><c:out value="${error}"/></div></c:if>
  <form action="${pageContext.request.contextPath}/register" method="post" class="a4-form" data-register>
    <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
    <label for="a4-name">Full name</label>
    <input id="a4-name" name="name" type="text" minlength="2" maxlength="100" autocomplete="name" value="<c:out value='${param.name}'/>" required placeholder="Your full name">
    <label for="a4-email">Email address</label>
    <input id="a4-email" name="email" type="email" maxlength="254" autocomplete="email" value="<c:out value='${param.email}'/>" required placeholder="you@example.com">
    <label for="a4-password">Password <small>(12–128 characters)</small></label>
    <div class="a4-password"><input id="a4-password" name="password" type="password" minlength="12" maxlength="128" autocomplete="new-password" required><button type="button" class="a4-show" data-toggle-password="a4-password" aria-label="Show password">Show</button></div>
    <label for="a4-confirm">Confirm password</label>
    <input id="a4-confirm" name="confirm" type="password" minlength="12" maxlength="128" autocomplete="new-password" required>
    <p class="a4-helper" role="status" data-password-message>Password values must match. Passwords are never stored as plaintext.</p>
    <button class="a4-submit" type="submit">Create account <svg class="mh-icon" aria-hidden="true" focusable="false"><use href="#mh-icon-arrow-right"/></svg></button>
  </form>
  <p class="a4-switch">Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
</section>

</main>
<%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body>
</html>
