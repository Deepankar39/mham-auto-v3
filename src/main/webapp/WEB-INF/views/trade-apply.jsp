<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!doctype html><html lang="en"><head>
<%@ include file="/WEB-INF/views/fragments/document-basics.jspf" %>
<title>Apply for a Trade Account | MHAM AUTO</title>
<link rel="icon" type="image/png" href="${pageContext.request.contextPath}/assets/images/mham-official-logo.png">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/catalog-preview.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/trade.css">
<script defer src="${pageContext.request.contextPath}/assets/js/catalog-preview.js"></script>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/premium.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/ui-polish.css">
<script defer src="${pageContext.request.contextPath}/assets/js/premium-interactions.js"></script>
<script defer src="${pageContext.request.contextPath}/assets/js/ui-polish.js"></script>
</head><body><%@ include file="/WEB-INF/views/fragments/catalog-header.jspf" %>
<main id="main-content" class="t9-shell mh-shell">
  <a class="t9-back" href="${pageContext.request.contextPath}/account">← My account</a>
  <div class="t9-hero"><p class="t9-eyebrow">MHAM AUTO / KUWAIT · TRADE BUYERS</p><h1>Apply for a business account</h1>
  <p>For garages, workshops, fleets and auto-parts professionals. This is a demonstration application; approval does not unlock wholesale pricing, credit or purchases.</p></div>
  <div class="t9-layout"><section class="t9-card">
    <h2>Company details</h2><p class="t9-muted">One application per customer account. Please use fictional or authorized test details for coursework.</p>
    <c:if test="${not empty errorMessage}"><div role="alert" class="t9-alert"><c:out value="${errorMessage}"/></div></c:if>
    <form action="${pageContext.request.contextPath}/trade/apply" method="post" class="t9-form">
      <input type="hidden" name="csrf" value="<c:out value='${csrfToken}'/>">
      <div class="t9-two"><div><label for="t9-company">Business / company name *</label><input id="t9-company" name="companyName" maxlength="160" minlength="2" value="<c:out value='${formCompany}'/>" required autocomplete="organization"></div>
      <div><label for="t9-contact">Contact person's name *</label><input id="t9-contact" name="contactName" maxlength="100" minlength="2" value="<c:out value='${formContact}'/>" required autocomplete="name"></div></div>
      <div class="t9-two"><div><label for="t9-phone">Kuwait mobile / contact *</label><input id="t9-phone" name="phone" type="tel" maxlength="16" placeholder="+965 5XXXXXXX" value="<c:out value='${formPhone}'/>" required autocomplete="tel"></div>
      <div><label for="t9-type">Business type *</label><select id="t9-type" name="businessType" required>
        <option value="">Select a type</option>
        <option value="GARAGE" ${formType eq 'GARAGE' ? 'selected' : ''}>Garage</option>
        <option value="WORKSHOP" ${formType eq 'WORKSHOP' ? 'selected' : ''}>Workshop</option>
        <option value="PARTS_RETAILER" ${formType eq 'PARTS_RETAILER' ? 'selected' : ''}>Parts retailer</option>
        <option value="FLEET" ${formType eq 'FLEET' ? 'selected' : ''}>Fleet operator</option>
        <option value="OTHER" ${formType eq 'OTHER' ? 'selected' : ''}>Other business</option>
      </select></div></div>
      <div class="t9-two"><div><label for="t9-gov">Kuwait governorate *</label><select id="t9-gov" name="governorate" required>
        <option value="">Select governorate</option>
        <option value="CAPITAL" ${formGov eq 'CAPITAL' ? 'selected' : ''}>Al Asimah (Capital)</option>
        <option value="HAWALLI" ${formGov eq 'HAWALLI' ? 'selected' : ''}>Hawalli</option>
        <option value="FARWANIYA" ${formGov eq 'FARWANIYA' ? 'selected' : ''}>Farwaniya</option>
        <option value="AHMADI" ${formGov eq 'AHMADI' ? 'selected' : ''}>Ahmadi</option>
        <option value="JAHRA" ${formGov eq 'JAHRA' ? 'selected' : ''}>Jahra</option>
        <option value="MUBARAK_AL_KABEER" ${formGov eq 'MUBARAK_AL_KABEER' ? 'selected' : ''}>Mubarak Al-Kabeer</option>
      </select></div><div><label for="t9-area">Business area *</label><input id="t9-area" name="area" maxlength="90" minlength="2" value="<c:out value='${formArea}'/>" placeholder="e.g. Shuwaikh" required></div></div>
      <label for="t9-message">About your business (optional)</label><textarea id="t9-message" name="message" rows="4" maxlength="500" placeholder="What parts do you typically source?"><c:out value="${formMessage}"/></textarea>
      <p class="t9-note">This application will start as <strong>PENDING</strong>. No real account verification, contracted trade price or payment facility is provided.</p>
      <button type="submit" class="t9-button">Submit demo application</button>
    </form>
  </section><aside class="t9-side"><h2>How it works</h2><ol><li>Submit basic company/contact details.</li><li>An administrator reviews the request.</li><li>See an demo review status in your account.</li></ol><p><strong>Demo boundary:</strong> no document upload, financial terms, credit, RFQ, special B2B role or real commerce onboarding.</p></aside></div>
</main><%@ include file="/WEB-INF/views/fragments/catalog-footer.jspf" %></body></html>
