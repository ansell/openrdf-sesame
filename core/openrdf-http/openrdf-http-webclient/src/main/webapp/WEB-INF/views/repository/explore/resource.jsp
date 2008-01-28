<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" />: <c:out value="${exploration.resource}" /></h1>
		
	<h2><fmt:message key="repository.explore.resource.explore" /></h2>
	
	<c:if test="${fn:length(asSubject) > 0}">
	<h3><fmt:message key="repository.explore.resource.withsubject" /></h3>
	<table class="data">
	<thead>
		<tr><th><fmt:message key="subject" /></th><th><fmt:message key="predicate" /></th><th><fmt:message key="object" /></th></tr>
	</thead>
	<tbody>
		<c:forEach var="subjStatement" items="${asSubject}">
		<tr>
			<td>this</td>
			<td><a href="${path}${view.path}?<orw:parameter name="resource" value="${subjStatement.predicate}" />">${subjStatement.predicate}</a></td>
			<c:choose>
				<c:when test="${orw:isResource(subjStatement.object)}">
			<td><a href="${path}${view.path}?<orw:parameter name="resource" value="${subjStatement.object}" />">${subjStatement.object}</td>
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
	<h3><fmt:message key="repository.explore.resource.withpredicate" /></h3>
	<table class="data">
	<thead>
		<tr><th><fmt:message key="subject" /></th><th><fmt:message key="predicate" /></th><th><fmt:message key="object" /></th></tr>
	</thead>
	<tbody>
		<c:forEach var="predStatement" items="${asPredicate}">
		<tr>
			<td><a href="${path}${view.path}?<orw:parameter name="resource" value="${predStatement.subject}" />">${predStatement.subject}</a></td>
			<td>this</td>
			<c:choose>
				<c:when test="${orw:isResource(predStatement.object)}">
			<td><a href="${path}${view.path}?<orw:parameter name="resource" value="${predStatement.object}" />">${predStatement.object}</td>
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
	<h3><fmt:message key="repository.explore.resource.withobject" /></h3>
	<table class="data">
	<thead>
		<tr><th><fmt:message key="subject" /></th><th><fmt:message key="predicate" /></th><th><fmt:message key="object" /></th></tr>
	</thead>
	<tbody>
		<c:forEach var="objStatement" items="${asObject}">
		<tr>
			<td><a href="${path}${view.path}?<orw:parameter name="resource" value="${objStatement.subject}" />">${objStatement.subject}</a></td>
			<td><a href="${path}${view.path}?<orw:parameter name="resource" value="${objStatement.predicate}" />">${objStatement.predicate}</a></td>
			<td>this</td>
		</tr>
		</c:forEach>
	</tbody>
	</table>
	</c:if>
	
	<h2><fmt:message key="repository.explore.resource.enter" /></h2>
	<form:form commandName="exploration" action="resource.view">
		<table class="dataentry">
		<tbody>
			<tr>
				<th><fmt:message key="repository.explore.resource" /></th>
				<td><form:input path="resource" /></td>
				<td><form:errors path="resource" cssClass="error" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td><input type="submit" value="<fmt:message key="form.submit" />" /></td>
				<td>&nbsp;</td>
			</tr>
		</tbody>
		</table>
	</form:form>

	<u:errors errors="${errors}" errorstitle="repository.error.title" />		
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
