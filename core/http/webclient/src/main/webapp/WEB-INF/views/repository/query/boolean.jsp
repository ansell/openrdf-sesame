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
	
	<form:form commandName="query">
	<table class="dataentry">
		<tbody>
			<tr>
				<th><fmt:message key="repository.query.querylanguage" /></th>
				<td>
					<form:select path="queryLanguage">
						<form:options items="${queryLanguages}" itemValue="qlId" itemLabel="qlName"/>
					</form:select>				
				</td>
				<td><form:errors path="queryLanguage" cssClass="error" /></td>
			</tr>
			<tr>
				<th><fmt:message key="repository.query.querystring" /></th>
				<td><form:textarea path="queryString" rows="6" cols="70" /></td>
				<td><form:errors path="queryString" cssClass="error" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					<form:checkbox path="includeInferred" /> <fmt:message key="repository.includeinferred" />
				</td>
				<td><form:errors path="includeInferred" cssClass="error" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td colspan="2"><input type="submit" value="Submit" /></td>
			</tr>
		</tbody>
	</table>	
	</form:form>
	
	<u:errors errors="${errors}" errorstitle="repository.error.title" />	
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf"%>

