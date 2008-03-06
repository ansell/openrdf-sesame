<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>
	
	<h2><fmt:message key="repository.explore.resource.enter" /></h2>
	<form method="GET" action="resource.view">
		<table class="dataentry">
		<tbody>
			<tr>
				<th><fmt:message key="repository.explore.resource" /></th>
				<td><input type="text" id="resource" name="resource" /></td>
			</tr>
		</tbody>
		</table>
	</form>
	
	<h2><fmt:message key="repository.explore.resource.explore" /></h2>	
	<p>
		<c:forEach var="clazz" items="${classes}">
			<a href="resource.view?<orw:parameter name="resource" value="${clazz}" />"><c:out value="${clazz}" /></a><br />
		</c:forEach>
	</p>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
