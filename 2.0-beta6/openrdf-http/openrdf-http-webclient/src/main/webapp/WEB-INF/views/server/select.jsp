<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>
	
	<p class="introduction">
		<fmt:message key="server.select.select_server_text" />
	</p>
	<form:form commandName="server">
		<table class="dataentry">
			<tbody class="fieldset">
				<tr>
					<td><form:radiobutton path="type" value="default"/></td>
					<th><fmt:message key="server.select.default_server" />:</th>
					<td>${server.defaultServerURL} <fmt:message key="server.select.default_server.may_not_exist" /></td>
					<td><form:errors path="type" cssClass="error" /></td>
				</tr>
				<tr>
					<td><form:radiobutton path="type" value="other"/></td>
					<th><fmt:message key="server.select.other_server" />:</th>
					<td><input id="location" name="location" type="text" size="50" /></td>
					<td>
						<form:errors path="location" cssClass="error" />
						<form:errors path="type" cssClass="error" />
					</td>
				</tr>
			</tbody>
			<tbody>
				<tr>
					<td><form:checkbox path="remember" /></td>
					<td colspan="2"><fmt:message key="server.select.remember" /></td>
					<td><form:errors path="remember" cssClass="error" /></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td colspan="3"><input type="submit" value="<fmt:message key="form.submit" />" /></th>
				</tr>
			</tbody>
		</table>
	</form:form>

	<u:errors errors="${errors}" errorstitle="repository.error.title" />	
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>

