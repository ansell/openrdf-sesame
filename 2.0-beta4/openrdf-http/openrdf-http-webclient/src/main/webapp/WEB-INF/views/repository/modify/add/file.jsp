<%@ include file="/WEB-INF/includes/components/page.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/head.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf"%>

<div id="content">
	<h1><fmt:message key="${view.parent.i18n}" /></h1>
	
	<h2><fmt:message key="${view.i18n}" /></h2>
	
	<form:form enctype="multipart/form-data">
<%@ include file="formstart.html.jspf" %>
			<tr>
				<th>Select the file containing the RDF data you wish to upload:</th>
				<td><input type="file" name="contents" /></td>
			</tr>
<%@ include file="formend.html.jspf" %>
	</form:form>
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf"%>
