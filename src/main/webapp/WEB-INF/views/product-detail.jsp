<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
    <title><c:out value="${product.title}"/> | MHAM AUTO Kuwait</title>
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/products.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/cart.css">
    <script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
    <script defer src="${pageContext.request.contextPath}/assets/js/products.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head>
<body>
<%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="p3-main">
    <div class="mh-shell p3-breadcrumb"><a href="${pageContext.request.contextPath}/products">← All products</a><span> / </span><c:out value="${product.category}"/></div>
    <div class="mh-shell"><div class="p3-notice" role="note"><strong>DEMO ENVIRONMENT:</strong> Compatibility and stock are not verified; no live checkout is available. Imported HTML descriptions are displayed as plain text.</div></div>
    <article class="mh-shell p3-detail">
        <div class="p3-gallery">
            <c:choose>
                <c:when test="${empty product.images}"><div class="p3-no-media"><img src="${pageContext.request.contextPath}/assets/images/parts-placeholder.svg" alt="No product image" width="140" height="140"><p>Image unavailable</p></div></c:when>
                <c:otherwise><c:forEach var="image" items="${product.images}"><div class="p3-gallery-frame"><img class="p3-media" loading="lazy" decoding="async" src="<c:out value='${image.safeUrl}'/>" alt="<c:out value='${image.alt}'/>" width="500" height="400"></div></c:forEach></c:otherwise>
            </c:choose>
        </div>
        <div class="p3-product-info">
            <p class="p3-kicker"><c:out value="${product.vendor}"/> / <c:out value="${product.category}"/></p>
            <h1><c:out value="${product.title}"/></h1>
            <p class="p3-detail-copy"><c:out value="${product.bodyText}"/></p>
            <h2>Variants and source data</h2>
            <c:forEach var="variant" items="${product.variants}">
                <section class="p3-variant">
                    <p><strong>SKU</strong> <c:out value="${variant.sku}"/></p>
                    <c:if test="${not empty variant.optionSummary}"><p><c:out value="${variant.optionSummary}"/></p></c:if>
                    <p class="p3-price"><c:out value="${variant.priceLabel}"/></p>
                    <p class="p3-muted"><c:out value="${variant.stockLabel}"/></p>
                    <c:choose>
                      <c:when test="${not empty sessionScope.authUser}">
                        <form action="${pageContext.request.contextPath}/cart" method="post" class="c5-add-form">
                          <input type="hidden" name="csrf" value="<c:out value='${cartCsrf}'/>">
                          <input type="hidden" name="action" value="add">
                          <input type="hidden" name="variantId" value="<c:out value='${variant.id}'/>">
                          <label>Quantity <input name="quantity" type="number" min="1" max="20" value="1" required></label>
                          <button type="submit">Add to demo cart</button>
                        </form>
                      </c:when>
                      <c:otherwise><a class="c5-sign-in" href="${pageContext.request.contextPath}/login">Sign in to add to cart</a></c:otherwise>
                    </c:choose>
                </section>
            </c:forEach>
            <div class="p3-disabled"><strong>Demo demonstration only.</strong><p>Adding to the cart does not reserve inventory or process payment. Prices with unverified source currency stay pending unless fictional demo KWD test prices have been explicitly assigned.</p></div>
            <a class="p3-outline" href="${pageContext.request.contextPath}/products">Continue browsing</a>
        </div>
    </article>
</main>
<%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body>
</html>
