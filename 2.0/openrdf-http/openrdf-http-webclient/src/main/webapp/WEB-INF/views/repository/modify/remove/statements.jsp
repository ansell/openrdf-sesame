<%@ include file="/WEB-INF/includes/components/page.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStart.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/head.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/bodyStart.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Header.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/Navigation.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/ContentHeader.html.jspf" %>

<div id="content">
	<h1><fmt:message key="${view.i18n}" /></h1>
	<p>
		<fmt:message key="repository.modify.remove.statements.intro" />
	</p>
	<ul>
		<li><fmt:message key="uri" /> <tt>&lt;http://foo.com/bar&gt;</tt></li>
		<li><fmt:message key="bnode" /> <tt>_:nodeID</tt></li>
		<li><fmt:message key="literal" /> <tt>"Hello"</tt>, <tt>"Hello"@en</tt> and <tt>"Hello"^^&lt;http://bar.com/foo&gt;</tt></li>
	</ul>
	<form:form>
		<table class="dataentry">
			<tbody>
				<tr>
					<th><fmt:message key="subject" /></th>
					<td><form:input path="subject" /></td>
					<td><form:errors path="subject" cssClass="error" /></td>
				</tr>
				<tr>
					<th><fmt:message key="predicate" /></th>
					<td><form:input path="predicate" /></td>
					<td><form:errors path="predicate" cssClass="error" /></td>
				</tr>	
				<tr>
					<th><fmt:message key="object" /></th>
					<td><form:input path="object" /></td>
					<td><form:errors path="object" cssClass="error" /></td>
				</tr>
				<tr>
					<td>&nbsp;</td>
					<td><input type="submit" value="<fmt:message key="form.submit" />" /></td>
					<td>&nbsp;</td>
				</tr>
			</tbody>
		</table>
	</form:form>	

	<u:errors errors="${errors}" errorstitle="repository.error.title" />	
</div>

<%@ include file="/WEB-INF/includes/components/Footer.html.jspf" %>

<%@ include file="/WEB-INF/includes/components/bodyStop.html.jspf" %>
<%@ include file="/WEB-INF/includes/components/htmlStop.html.jspf" %>
