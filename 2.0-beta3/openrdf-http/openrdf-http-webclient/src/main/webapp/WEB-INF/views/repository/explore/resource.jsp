<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>
	
	<h2><c:out value="${exploration.resource}" /></h2>
	
	<c:if test="${fn:length(asSubject) > 0}">
	<h3>Statements with this resource as subject</h3>
	<table class="data">
	<thead>
		<tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>
	</thead>
	<tbody>
		<c:forEach var="subjStatement" items="${asSubject}">
		<tr>
			<td>this</td>
			<td><a href="${path}/${view.path}?<orw:parameter name="resource" value="${subjStatement.predicate}" />">${subjStatement.predicate}</a></td>
			<c:choose>
				<c:when test="${orw:isResource(subjStatement.object)}">
			<td><a href="${path}/${view.path}?<orw:parameter name="resource" value="${subjStatement.object}" />">${subjStatement.object}</td>
				</c:when>
				<c:otherwise>
			<td>${subjStatement.object}</td>
				</c:otherwise>
			</c:choose>
		</tr>
		</c:forEach>
	</tbody>
	</table>
	</c:if>

	<c:if test="${fn:length(asPredicate) > 0}">
	<h3>Statements with this resource as predicate</h3>
	<table class="data">
	<thead>
		<tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>
	</thead>
	<tbody>
		<c:forEach var="predStatement" items="${asPredicate}">
		<tr>
			<td><a href="${path}/${view.path}?<orw:parameter name="resource" value="${predStatement.subject}" />">${predStatement.subject}</a></td>
			<td>this</td>
			<c:choose>
				<c:when test="${orw:isResource(predStatement.object)}">
			<td><a href="${path}/${view.path}?<orw:parameter name="resource" value="${predStatement.object}" />">${predStatement.object}</td>
				</c:when>
				<c:otherwise>
			<td>${predStatement.object}</td>
				</c:otherwise>
			</c:choose>
		</tr>
		</c:forEach>
	</tbody>
	</table>
	</c:if>

	<c:if test="${fn:length(asObject) > 0}">
	<h3>Statements with this resource as object</h3>
	<table class="data">
	<thead>
		<tr><th>Subject</th><th>Predicate</th><th>Object</th></tr>
	</thead>
	<tbody>
		<c:forEach var="objStatement" items="${asObject}">
		<tr>
			<td><a href="${path}/${view.path}?<orw:parameter name="resource" value="${objStatement.subject}" />">${objStatement.subject}</a></td>
			<td><a href="${path}/${view.path}?<orw:parameter name="resource" value="${objStatement.predicate}" />">${objStatement.predicate}</a></td>
			<td>this</td>
		</tr>
		</c:forEach>
	</tbody>
	</table>
	</c:if>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
