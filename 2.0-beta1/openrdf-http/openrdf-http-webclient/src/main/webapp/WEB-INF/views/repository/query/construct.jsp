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
				<th>Query Language:</th>
				<td>
					<form:select path="queryLanguage">
						<form:options items="${queryLanguages}" itemValue="qlId" itemLabel="qlName"/>
					</form:select>				
				</td>
				<td><form:errors path="queryLanguage" cssClass="error" /></td>
			</tr>
			<tr>
				<th>Query:</th>
				<td><form:textarea path="query" rows="6" cols="70" /></td>
				<td><form:errors path="query" cssClass="error" /></td>
			</tr>
			<tr>
				<th>Result format:</th>
				<td>
					<form:select path="resultFormat">
						<form:options items="${resultFormats}" itemValue="rfId" itemLabel="rfName"/>
					</form:select>				
				</td>
				<td><form:errors path="resultFormat" cssClass="error" /></td>
			</tr>
			<tr>
				<td>&nbsp;</td>
				<td>
					<form:checkbox path="includeInferred" /> Include inferred statements
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
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf"%>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf"%>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf"%>