<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
    <meta name="robots" content="noindex, nofollow">
    <title>Page unavailable | MHAM AUTO</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/error.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head>
<body>
    <main class="error-shell">
        <a class="error-brand" href="${pageContext.request.contextPath}/">MHAM <strong>AUTO</strong></a>
        <div class="error-panel">
            <span class="error-eyebrow">DEMO ENVIRONMENT · KUWAIT</span>
            <h1>Page unavailable</h1>
            <p>The page could not be displayed. Check the address or return to the storefront.</p>
            <a class="error-action" href="${pageContext.request.contextPath}/">Return home <svg class="mh-icon" aria-hidden="true" focusable="false"><use href="#mh-icon-arrow-right"/></svg></a>
        </div>
        <small>MHAM AUTO · Demo demonstration</small>
    </main>
</body>
</html>
