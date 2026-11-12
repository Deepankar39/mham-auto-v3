<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><%@ include file="/WEB-INF/views/fragments/admin-head.jspf" %><title>Product Editor | MHAM AUTO</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head>
<body><%@ include file="/WEB-INF/views/fragments/admin-shell-start.jspf" %><main id="main-content" class="a8-main mh-shell">
<div class="a8-top"><div><p class="a8-kicker">PRODUCT EDITOR</p><h1><c:choose><c:when test="${not empty product}">Edit product</c:when><c:otherwise>Add product</c:otherwise></c:choose></h1><p>The first sellable variant is editable here; multi-variant import management remains source-driven.</p></div><a href="${pageContext.request.contextPath}/admin/products">← Products</a></div>

<c:if test="${saved}"><p class="a8-success" role="status">Product saved successfully.</p></c:if>
<form method="post" action="${pageContext.request.contextPath}/admin/product" class="a8-panel a8-form">
<input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>"><c:if test="${not empty product}"><input type="hidden" name="id" value="<c:out value='${product.id}'/>"></c:if>
<div class="a8-form-grid">
 <div><label for="a8-handle">Product handle (unique)</label><c:choose><c:when test="${not empty product}"><input id="a8-handle" value="<c:out value='${product.handle}'/>" readonly><small>Stable import key. Cannot be changed in this editor.</small></c:when><c:otherwise><input id="a8-handle" name="handle" maxlength="191" pattern="[a-z0-9]+(-[a-z0-9]+)*" placeholder="example-brake-pad" required></c:otherwise></c:choose></div>
 <div><label for="a8-title">Product name</label><input id="a8-title" name="title" maxlength="512" required value="<c:out value='${product.title}'/>"></div>
 <div><label for="a8-vendor">Vendor / brand</label><input id="a8-vendor" name="vendor" maxlength="190" required value="<c:out value='${product.vendor}'/>" placeholder="MHAM AUTO"></div>
 <div><label for="a8-category">Category</label><select id="a8-category" name="categoryId"><option value="">Uncategorised</option><c:forEach var="cat" items="${categories}"><option value="<c:out value='${cat.id}'/>" ${product.category eq cat.name ? 'selected="selected"' : ''}><c:out value="${cat.name}"/></option></c:forEach></select></div>
 <div><label for="a8-status">Status</label><select id="a8-status" name="status"><option value="draft" ${product.status eq 'draft' ? 'selected="selected"' : ''}>Draft</option><option value="active" ${product.status eq 'active' ? 'selected="selected"' : ''}>Active</option><option value="archived" ${product.status eq 'archived' ? 'selected="selected"' : ''}>Archived (soft deactivate)</option></select></div>
 <div><label for="a8-published">Publish to public catalogue</label><select id="a8-published" name="published"><option value="false" ${not empty product and not product.published ? 'selected="selected"' : ''}>No</option><option value="true" ${not empty product and product.published ? 'selected="selected"' : ''}>Yes</option></select></div>
 <div><label for="a8-sku">First-variant SKU (unique)</label><input id="a8-sku" name="sku" required maxlength="191" value="<c:out value='${product.sku}'/>"></div>
 <div><label for="a8-stock">Sample quantity (unverified)</label><input id="a8-stock" name="stock" type="number" min="0" max="99999999" step="1" value="<c:out value='${product.sourceStock}'/>"><small>Not a verified warehouse quantity and does not allocate stock.</small></div>
 <div><label for="a8-price">Fictional demo price (KWD)</label><input id="a8-price" name="demoPrice" inputmode="decimal" pattern="[0-9]+(\.[0-9]{1,3})?" maxlength="18" value="<c:out value='${product.demoPrice}'/>"><small>Optional, up to 3 decimals. Never copy an unverified CSV price.</small></div>
 <c:if test="${empty product}"><div><label for="a8-image">Shopify CDN image URL (optional)</label><input id="a8-image" type="url" name="imageUrl" maxlength="2048" placeholder="https://cdn.shopify.com/..."></div></c:if>
</div>
<label for="a8-desc">Plain-text description <c:if test="${not empty product}">(optional replacement)</c:if></label>
<textarea id="a8-desc" name="description" rows="5" maxlength="10000" placeholder="Optional description. Leave blank on edit to preserve imported HTML."></textarea>
<label class="a8-check"><input type="checkbox" name="confirmFictional" value="yes"> I confirm any entered KWD amount is explicitly fictional Demonstration V1 test pricing, not a verified supplier price.</label>
<div class="a8-actions"><button type="submit">Save product &amp; sample stock</button><a href="${pageContext.request.contextPath}/admin/products">Cancel</a></div>
<p class="a8-warning">Existing order snapshots will not change. Soft archive products rather than deleting records referenced by carts or orders.</p>
</form></main><%@ include file="/WEB-INF/views/fragments/admin-shell-end.jspf" %></body></html>
