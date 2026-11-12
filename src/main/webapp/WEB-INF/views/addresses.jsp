<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Saved Kuwait Addresses | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/checkout.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body>
<%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="c6-shell mh-shell">
  <div class="c6-crumb"><a href="${pageContext.request.contextPath}/account">Account</a> / Kuwait addresses</div>
  <p class="c6-kicker">06 / KUWAIT ADDRESS MANAGEMENT</p>
  <h1>Delivery address book<span>.</span></h1>
  <p class="c6-lede">Save up to 10 Kuwait addresses for demonstration orders. Addresses are private to your customer account.</p>
  <div class="c6-demo-banner"><strong>DEMO MODE</strong> — Saved addresses are used for sample order records only. No courier booking or physical delivery.</div>
  <c:if test="${not empty addressNotice}"><p role="status" class="c6-success"><c:out value="${addressNotice}"/></p></c:if>
  <div class="c6-columns">
    <section class="c6-panel"><h2><c:choose><c:when test="${not empty editAddress}">Edit your address</c:when><c:otherwise>Add Kuwait address</c:otherwise></c:choose></h2>
      <form method="post" action="${pageContext.request.contextPath}/addresses" class="c6-form">
        <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
        <input type="hidden" name="action" value="save">
        <c:if test="${not empty editAddress}"><input type="hidden" name="addressId" value="<c:out value='${editAddress.id}'/>"></c:if>
        <div class="c6-two"><label>Recipient name *<input name="recipientName" required minlength="2" maxlength="100" autocomplete="name" value="<c:out value='${editAddress.recipientName}'/>"></label>
          <label>Kuwait phone number *<input name="phone" required maxlength="18" placeholder="+965 5XXXXXXX" autocomplete="tel" value="<c:out value='${editAddress.phone}'/>"></label></div>
        <div class="c6-two"><label>Governorate *<select name="governorate" required>
            <option value="">Choose governorate</option>
            <c:forEach items="${governorateOptions}" var="code"><option value="<c:out value='${code}'/>" <c:if test="${editAddress.governorate == code}">selected</c:if>><c:choose><c:when test="${code == 'CAPITAL'}">Al Asimah (Capital)</c:when><c:when test="${code == 'MUBARAK_AL_KABEER'}">Mubarak Al-Kabeer</c:when><c:otherwise><c:out value="${code}"/></c:otherwise></c:choose></option></c:forEach>
          </select></label>
          <label>Area *<input name="area" required minlength="2" maxlength="90" placeholder="e.g. Salmiya" value="<c:out value='${editAddress.area}'/>"></label></div>
        <div class="c6-two"><label>Block *<input name="block" required maxlength="20" placeholder="e.g. 10" value="<c:out value='${editAddress.block}'/>"></label>
        <label>Street *<input name="street" required minlength="2" maxlength="120" value="<c:out value='${editAddress.street}'/>"></label></div>
        <div class="c6-two"><label>Building / house *<input name="building" required maxlength="60" value="<c:out value='${editAddress.building}'/>"></label>
        <label>Floor / apartment (optional)<input name="floorDetail" maxlength="100" value="<c:out value='${editAddress.floorDetail}'/>"></label></div>
        <label>Directions (optional)<textarea name="directions" maxlength="250" rows="3"><c:out value="${editAddress.directions}"/></textarea></label>
        <button class="c6-primary" type="submit">Save address</button>
        <c:if test="${not empty editAddress}"><a class="c6-text-link" href="${pageContext.request.contextPath}/addresses">Cancel editing</a></c:if>
      </form>
    </section>
    <aside class="c6-panel"><h2>Saved addresses (<c:out value="${addressList.size()}"/>)</h2>
      <c:choose><c:when test="${empty addressList}"><p class="c6-muted">No saved addresses yet. Add one to use it at checkout.</p></c:when>
      <c:otherwise><c:forEach items="${addressList}" var="a"><div class="c6-address">
        <strong><c:out value="${a.recipientName}"/></strong>
        <p><c:out value="${a.phone}"/></p><p><c:out value="${a.governorateLabel}"/> · <c:out value="${a.summary}"/></p>
        <div class="c6-inline"><a href="${pageContext.request.contextPath}/addresses?edit=<c:out value='${a.id}'/>">Edit</a>
          <form method="post" action="${pageContext.request.contextPath}/addresses" onsubmit="return confirm('Remove this saved address? Existing demo orders keep their address snapshot.');">
            <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>"><input type="hidden" name="action" value="delete"><input type="hidden" name="addressId" value="<c:out value='${a.id}'/>"><button type="submit" class="c6-danger">Remove</button>
          </form></div></div></c:forEach></c:otherwise></c:choose>
      <a class="c6-secondary" href="${pageContext.request.contextPath}/checkout">Review demo checkout</a>
    </aside>
  </div>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %>
</body></html>
