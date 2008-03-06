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
			<tr><th><fmt:message key="repository.overview.id" />:</th><td>${repositoryInfo.id}</td></tr>
			<tr><th><fmt:message key="repository.overview.description" />:</th><td>${repositoryInfo.description}</td></tr>
			<tr><th><fmt:message key="repository.overview.location" />:</th><td><c:out value="${repositoryInfo.location}"/></td></tr>
		</tbody>
	</table>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
