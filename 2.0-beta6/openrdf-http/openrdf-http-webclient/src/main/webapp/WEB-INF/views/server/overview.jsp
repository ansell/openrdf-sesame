<%@ include file="/WEB-INF/includes/components/page.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/head.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>
	<h2><fmt:message key="server.overview.identification" /></h2>
	<table class="simple">
		<tbody>
			<tr><th><fmt:message key="server.overview.location" />:</th><td><c:out value="${server.location}"/></td></tr>
			<tr><th><fmt:message key="server.overview.version" />:</th><td>TODO</td></tr>
		</tbody>
	</table>

	<h2><fmt:message key="server.overview.repositories" /></h2>
	<table class="data">
		<thead>
			<tr><th><img src="${path}/images/view.png" alt="<fmt:message key="readable" />" title="<fmt:message key="readable" />"/></th><th><img src="${path}/images/edit.png" alt="<fmt:message key="writeable" />" title="<fmt:message key="writeable" />"/></th><th><fmt:message key="repository.overview.id" /></th><th><fmt:message key="repository.overview.description" /></th><th><fmt:message key="repository.overview.location" /></th></tr>
		</thead>
		<tbody>
		<c:forEach items="${server.repositories}" var="repository">
			<tr>
				<td>
			<c:choose>
				<c:when test="${repository.value.readable}"><img src="${path}/images/affirmative.png" alt="<fmt:message key="affirmative" />" title="<fmt:message key="affirmative" />"/></c:when>
				<c:otherwise><img src="${path}/images/negative.png" alt="<fmt:message key="negative" />" title="<fmt:message key="negative" />"/></c:otherwise>
			</c:choose>
				</td>
				<td>
			<c:choose>
				<c:when test="${repository.value.writable}"><img src="${path}/images/affirmative.png" alt="<fmt:message key="affirmative" />" title="<fmt:message key="affirmative" />"/></c:when>
				<c:otherwise><img src="${path}/images/negative.png" alt="<fmt:message key="negative" />" title="<fmt:message key="negative" />"/></c:otherwise>
			</c:choose>
				</td>
				<td><a href="<c:url value="/repository/overview.view?id=${repository.value.id}" />"><c:out value="${repository.value.id}" /></a></td>
				<td><c:out value="${repository.value.description}" default="-" /></td>
				<td><c:out value="${repository.value.location}" /></td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf"%>

