<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html>
<html lang="en">
<head>
    <%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
    <title>Shop Spare Parts | MHAM AUTO Kuwait</title>
    <meta name="description" content="Browse MHAM AUTO automotive spare parts with product search, brand filters and transparent availability notes.">
    <link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/products.css">
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
    <section class="p3-intro mh-shell">
        <div><p class="p3-kicker">MHAM AUTO · PARTS DISCOVERY</p>
        <h1>Find the right <em>part.</em></h1>
        <p>Search our sample catalogue by product name or SKU. Product availability and compatibility are not verified.</p></div>
        <a class="p3-outline" href="${pageContext.request.contextPath}/catalog">View category preview</a>
    </section>
    <div class="mh-shell"><div class="p3-notice" role="note"><strong>DEMO ENVIRONMENT:</strong> Products may be synthetic. No real orders, verified vehicle fitment, stock commitments or payments. Displayed KWD prices are either source-confirmed or explicitly assigned as fictional demo environment test prices.</div></div>
    <section class="mh-shell p3-content" aria-label="Product catalogue">
        <form class="p3-filters" action="${pageContext.request.contextPath}/products" method="get" role="search">
            <div class="p3-filter-title"><h2>Refine results</h2><a href="${pageContext.request.contextPath}/products">Reset all</a></div>
            <label for="p3-q">Search name or SKU</label>
            <input id="p3-q" type="search" name="q" maxlength="80" placeholder="e.g. wheel hub" value="<c:out value='${filter.query}'/>">
            <label for="p3-vendor">Vendor / manufacturer</label>
            <select id="p3-vendor" name="vendor">
                <option value="">All vendors</option>
                <c:forEach var="v" items="${vendors}">
                    <option value="<c:out value='${v.id}'/>" ${filter.vendorId eq v.id ? 'selected="selected"' : ''}><c:out value="${v.name}"/></option>
                </c:forEach>
            </select>
            <label for="p3-category">Category</label>
            <select id="p3-category" name="category">
                <option value="">All categories</option>
                <option value="uncategorized" ${filter.uncategorized ? 'selected="selected"' : ''}>Uncategorised / missing source</option>
                <c:forEach var="categ" items="${categories}">
                    <option value="<c:out value='${categ.id}'/>" ${filter.categoryId eq categ.id ? 'selected="selected"' : ''}><c:out value="${categ.name}"/></option>
                </c:forEach>
            </select>
            <label for="p3-sort">Sort by</label>
            <select id="p3-sort" name="sort">
                <option value="relevance" ${filter.sort eq 'relevance' ? 'selected="selected"' : ''}>Newest import IDs</option>
                <option value="title_asc" ${filter.sort eq 'title_asc' ? 'selected="selected"' : ''}>Name A–Z</option>
                <option value="title_desc" ${filter.sort eq 'title_desc' ? 'selected="selected"' : ''}>Name Z–A</option>
                <option value="price_asc" ${filter.sort eq 'price_asc' ? 'selected="selected"' : ''}>Price low to high</option>
                <option value="price_desc" ${filter.sort eq 'price_desc' ? 'selected="selected"' : ''}>Price high to low</option>
            </select>
            <button type="submit">Apply filters <svg class="mh-icon" aria-hidden="true" focusable="false"><use href="#mh-icon-arrow-right"/></svg></button>
        </form>
        <div class="p3-results">
            <div class="p3-result-heading"><div><p class="p3-kicker">EXPLORE PARTS</p><h2>Product catalogue</h2></div><span class="p3-counter"><c:out value="${listing.total}"/> results</span></div>
            <c:choose>
                <c:when test="${listing.total eq 0}">
                    <div class="p3-empty"><h3>No matching products</h3><p>Try another part name, SKU prefix or filter. Only published active products for the Kuwait market are shown.</p><a href="${pageContext.request.contextPath}/products">Clear filters</a></div>
                </c:when>
                <c:otherwise>
                    <p class="p3-range">Showing <c:out value="${listing.firstItem}"/>–<c:out value="${listing.lastItem}"/> of <c:out value="${listing.total}"/></p>
                    <div class="p3-grid">
                    <c:forEach var="item" items="${listing.cards}">
                        <article class="p3-card">
                            <a class="p3-card-image" href="${pageContext.request.contextPath}<c:out value='${item.url}'/>">
                                <c:choose>
                                    <c:when test="${not empty item.imageUrl}"><img class="p3-media" src="<c:out value='${item.imageUrl}'/>" alt="<c:out value='${item.title}'/>" loading="lazy" decoding="async" width="300" height="220"></c:when>
                                    <c:otherwise><img src="${pageContext.request.contextPath}/assets/images/parts-placeholder.svg" alt="Image unavailable" loading="lazy" width="160" height="160"></c:otherwise>
                                </c:choose>
                            </a>
                            <div class="p3-card-body">
                                <p class="p3-meta"><c:out value="${item.vendor}"/> · <c:out value="${item.category}"/></p>
                                <h3><a href="${pageContext.request.contextPath}<c:out value='${item.url}'/>"><c:out value="${item.title}"/></a></h3>
                                <p class="p3-sku">SKU: <c:out value="${item.sku}"/></p>
                                <p class="p3-price"><c:out value="${item.priceLabel}"/></p>
                                <a class="p3-view" href="${pageContext.request.contextPath}<c:out value='${item.url}'/>">View details <svg class="mh-icon" aria-hidden="true" focusable="false"><use href="#mh-icon-arrow-right"/></svg></a>
                            </div>
                        </article>
                    </c:forEach>
                    </div>
                </c:otherwise>
            </c:choose>
            <nav class="p3-pagination" aria-label="Catalogue pages">
                <c:if test="${listing.hasPrevious}"><a rel="prev" href="${pageContext.request.contextPath}<c:out value='${filter.previousHref}'/>">← Previous</a></c:if>
                <span>Page <c:out value="${listing.page}"/></span>
                <c:if test="${listing.hasNext}"><a rel="next" href="${pageContext.request.contextPath}<c:out value='${filter.nextHref}'/>">Next</a></c:if>
            </nav>
        </div>
    </section>
</main>
<%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body>
</html>
