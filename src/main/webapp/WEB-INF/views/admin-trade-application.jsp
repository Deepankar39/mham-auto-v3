<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %><%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head><%@ include file="/WEB-INF/views/fragments/admin-head.jspf" %><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/trade.css"><title>Review Trade Application | MHAM AUTO</title><link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head>
<body><%@ include file="/WEB-INF/views/fragments/admin-shell-start.jspf" %><main id="main-content" class="a8-main mh-shell">
<div class="a8-top"><div><p class="a8-kicker">TRADE APPLICATION #<c:out value="${application.id}"/></p><h1><c:out value="${application.companyName}"/></h1><p>Submitted <c:out value="${application.createdAt}"/></p></div><a href="${pageContext.request.contextPath}/admin/trade-applications">← All applications</a></div>

<c:if test="${saved}"><div role="status" class="a8-success">Demonstration application review saved.</div></c:if>
<div class="a8-grid"><section class="a8-panel"><h2>Business request</h2><dl class="t9-definition">
<dt>Company</dt><dd><c:out value="${application.companyName}"/></dd><dt>Applicant account</dt><dd><c:out value="${application.applicantEmail}"/></dd>
<dt>Contact</dt><dd><c:out value="${application.contactName}"/></dd><dt>Contact number</dt><dd><c:out value="${application.phone}"/></dd>
<dt>Business type</dt><dd><c:out value="${application.businessType}"/></dd><dt>Governorate / area</dt><dd><c:out value="${application.governorate}"/> / <c:out value="${application.area}"/></dd>
<dt>Applicant message</dt><dd class="t9-wrap"><c:out value="${application.applicantNote}" default="—"/></dd>
</dl></section><section class="a8-panel"><h2>Admin review</h2><p>Current status: <span class="t9-pill t9-${application.status}"><c:out value="${application.status}"/></span></p>
<c:choose><c:when test="${application.pending}"><form method="post" action="${pageContext.request.contextPath}/admin/trade-application" class="a8-form">
<input type="hidden" name="id" value="<c:out value='${application.id}'/>"><input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
<label for="t9-review">Review decision *</label><select id="t9-review" name="decision" required><option value="">Choose a decision</option><option value="APPROVED">Approve demo application</option><option value="REJECTED">Reject demo application</option></select>
<label for="t9-review-note">Internal review note (not visible to applicant)</label><textarea id="t9-review-note" name="adminNote" rows="4" maxlength="500" placeholder="Optional internal explanation"></textarea>
<button type="submit">Save review status</button></form></c:when><c:otherwise><p>This application has been reviewed. Demonstration V1 permits one final decision only.</p><p>Reviewed: <c:out value="${application.reviewedAt}"/></p><p class="t9-wrap">Internal note: <c:out value="${application.adminNote}" default="—"/></p></c:otherwise></c:choose>
<p class="a8-warning">No trade price, credit, purchasing or organization permissions will be activated.</p></section></div>
</main><%@ include file="/WEB-INF/views/fragments/admin-shell-end.jspf" %></body></html>
