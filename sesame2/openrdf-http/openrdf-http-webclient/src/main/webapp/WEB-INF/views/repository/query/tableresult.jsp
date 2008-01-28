<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="repository.query.overview.title" /></h1>

	<p>
		<a href="${path}${view.path}"><fmt:message key="repository.query.another" /></a>
	</p>
	
	<h2><fmt:message key="repository.query.result.title" /></h2>

	<table class="data">
		<thead>
			<tr>
			<c:forEach var="bindingName" items="${bindingNames}">
				<th>${bindingName}</th>
			</c:forEach>
			</tr>
		</thead>
		<tbody>
			<orw:forEach var="solution" items="${solutions}">
			<tr>
				<c:forEach var="binding" items="${orw:bindingsInOrder(bindingNames, solution)}">
				<td>
					<c:choose>
						<c:when test="${binding != null}">
							${binding.value}
						</c:when>
						<c:otherwise>
							<span class="disabled"><fmt:message key="repository.query.result.null" /></span>
						</c:otherwise>
					</c:choose>
				</td>
				</c:forEach>
			</tr>
			</orw:forEach>
		</tbody>
	</table>
	
	<p>
		<a href="${path}${view.path}"><fmt:message key="repository.query.another" /></a>
	</p>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
