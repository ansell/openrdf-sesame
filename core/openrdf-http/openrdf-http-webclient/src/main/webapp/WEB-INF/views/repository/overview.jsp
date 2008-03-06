<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>

	<h2><fmt:message key="repository.overview.identification" /></h2>
	<table class="simple">
		<tbody>
			<tr><th><fmt:message key="repository.overview.id" />:</th><td>${repository.id}</td></tr>
			<tr><th><fmt:message key="repository.overview.description" />:</th><td>${repository.description}</td></tr>
			<tr><th><fmt:message key="repository.overview.location" />:</th><td><c:out value="${repository.location}"/></td></tr>
		</tbody>
	</table>

	<h2><fmt:message key="repository.overview.namespaces" /></h2>
	<c:set var="namespaces" value="${repository.namespaces}" />
	<c:choose>
		<c:when test="${fn:length(namespaces) > 0}">
	<table class="data">
		<thead>
			<tr>
				<th><fmt:message key="repository.overview.namespace.prefix" /></th>
				<th><fmt:message key="repository.overview.namespace" /></th>
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
		<fmt:message key="repository.overview.namespaces.none" />
	</p>
		</c:otherwise>
	</c:choose>

	<h2><fmt:message key="repository.overview.contexts" /></h2>
	<c:set var="contexts" value="${repository.contexts}" />
	<c:choose>
		<c:when test="${fn:length(contexts) > 0}">
	<table class="data">
		<thead>
			<tr>
				<th><fmt:message key="repository.overview.context" /></th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="context" items="${contexts}">
			<tr>
				<td>${context}</td>
			</tr>
			</c:forEach>
		</tbody>
	</table>
		</c:when>
		<c:otherwise>
	<p>
		<fmt:message key="repository.overview.contexts.none" />
	</p>
		</c:otherwise>
	</c:choose>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
