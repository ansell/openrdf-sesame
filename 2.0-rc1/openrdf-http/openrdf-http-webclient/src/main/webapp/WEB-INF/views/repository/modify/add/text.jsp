<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.parent.i18n}" /></h1>
	
	<h2><fmt:message key="${view.i18n}" /></h2>
	
	<form:form>
<%@ include file="formstart.html.jspf" %>
			<tr>
				<th><fmt:message key="repository.modify.add.text.contents" /></th>
				<td><form:textarea path="contents" rows="6" cols="70" /></td>
				<td><form:errors path="contents" cssClass="error" /></td>
			</tr>
<%@ include file="formend.html.jspf" %>
	</form:form>

	<u:errors errors="${errors}" errorstitle="repository.error.title" />	
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
