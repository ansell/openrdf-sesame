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

	<pre class="queryResult">
		<c:choose>
		<c:when test="${answer}">
			<fmt:message key="repository.query.boolean.true" />
		</c:when>
		<c:otherwise>
			<fmt:message key="repository.query.boolean.false" />
		</c:otherwise>
		</c:choose>
	</pre>

	<p>
		<a href="${path}${view.path}"><fmt:message key="repository.query.another" /></a>
	</p>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
