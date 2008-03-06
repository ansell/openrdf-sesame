<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<c:set var="titlekey" value="overview.title" />
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<h1><fmt:message key="${titlekey}" /></h1>

<h2><fmt:message key="overview.select_server" /></h2>
<p>
	<fmt:message key="overview.select_server_text" />
</p>
<form:form commandName="server">
	<table class="dataentry">
		<tr>
			<td><img src="<%=path%>/images/server.png" alt="<fmt:message key="overview.local_server" />" /></td>
			<td><form:radiobutton path="type" value="local"/></td>
			<td colspan="2"><fmt:message key="overview.local_server" /></td>
			<td><form:errors path="type" cssClass="error" /></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
			<th><fmt:message key="overview.server_url" /></th>
			<td colspan="2">${server.localServerURL} <fmt:message key="overview.local_server.may_not_exist" /></td>
		</tr>
		<tr>
			<td><img src="<%=path%>/images/server_network.png" alt="<fmt:message key="overview.remote_server" />" /></td>
			<td><form:radiobutton path="type" value="remote"/></td>
			<td colspan="2"><fmt:message key="overview.remote_server" /></td>
			<td><form:errors path="type" cssClass="error" /></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
			<th><fmt:message key="overview.server_url" /></th>
			<td><input id="serverURL" name="serverURL" type="text" size="40" /></td>
			<td><form:errors path="serverURL" cssClass="error" /></td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td><form:checkbox path="useAlways" /></td>
			<td colspan="2"><fmt:message key="overview.always_use_server" /></td>
			<td><form:errors path="useAlways" cssClass="error" /></td>
		</tr>
		<tr>
			<td colspan="2">&nbsp;</td>
			<td colspan="3"><input type="submit" value="<fmt:message key="form.submit" />" /></th>
		</tr>
	</table>
</form:form>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>

