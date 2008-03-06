<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<c:set var="titlekey" value="server.title" />
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<h1><fmt:message key="${titlekey}" /></h1>
<p>
<fmt:message key="server.url" /> <c:out value="${sesameServer.serverURL}"/>
</p>
<p>
<fmt:message key="server.repositories" />
</p>
<ul>
<c:forEach items="${sesameServer.repositories}" var="repository">
  <li><a href="<c:url value="repository/overview.view?location=${repository.value.location}&description=${repository.value.description}"/>"><c:out value="${repository.value.description}"/></a></li>
</c:forEach>
</ul>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>

