<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>

	<c:if test="${param['actionResult'] != null}">
	<p class="introduction">
		<fmt:message key="${param['actionResult']}" />
	</p>
	</c:if>
	
	<p class="introduction">
		<fmt:message key="repository.modify.overview.text" />
	</p>
	
	<c:forEach var="actiongroup" items="${view.parent.groups}">
		<c:if test="${!actiongroup.hidden}">
	<h2><fmt:message key="${actiongroup.i18n}" /></h2>
	<ul>
		<c:forEach var="actionview" items="${actiongroup.views}">
			<c:if test="${!actionview.hidden}">
			<c:choose>
				<c:when test="${actionview.enabled}">
		<li><a href="${path}/${actionview.path}"><fmt:message key="${actionview.i18n}" /></a></li>
				</c:when>
				<c:otherwise>
		<li class="disabled"><fmt:message key="${actionview.i18n}" /></li>
				</c:otherwise>
			</c:choose>
			</c:if>
		</c:forEach>
	</ul>
		</c:if>
	</c:forEach>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
