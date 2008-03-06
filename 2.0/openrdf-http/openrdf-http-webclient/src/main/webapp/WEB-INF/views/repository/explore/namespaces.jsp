<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>

	<h2><fmt:message key="repository.explore.namespaces" /></h2>
	<c:choose>
		<c:when test="${fn:length(namespaces) > 0}">
	<table class="data">
		<thead>
			<tr>
				<th><fmt:message key="repository.explore.namespace.prefix" /></th>
				<th><fmt:message key="repository.explore.namespace" /></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="namespace" items="${namespaces}">
			<tr>
				<td>${namespace.prefix}</td>
				<td>${namespace.name}</td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
		</c:when>
		<c:otherwise>
	<p>
		<fmt:message key="repository.explore.namespaces.none" />
	</p>
		</c:otherwise>
	</c:choose>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
