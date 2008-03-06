<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<c:set var="titlekey" value="repository.overview.title" />
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<h1><fmt:message key="${titlekey}" /></h1>
<h2>${repository.description}</h2>
<p>
Use the toolbar above to query the store using a Web form. You can also access the store through 
the Sesame 2 HTTP REST API directly, by going to <c:out value="${repository.location}"/> and adding URL parameters as described in the <a href="http://www.openrdf.org/doc/sesame2/system/ch08.html">REST API documentation</a>.
</p>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
