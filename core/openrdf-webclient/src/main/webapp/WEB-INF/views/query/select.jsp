<%@ include file="/WEB-INF/jsp/includes/toolbar-header.jsp" %>

<form method="post">
  <table>
    <spring:bind path="query.query">
    <tr>
      <td><c:out value="${param.ql}"/> SELECT Query:</td>
      </tr>
      <tr>
      <td>
        <textarea name="query" rows="6" cols="70">
		</textarea>
      </td>
    </tr>
    <tr>
      <td>
        <font color="red"><c:out value="${status.errorMessage}"/></font>
      </td>
    </tr>
    </spring:bind>
    <spring:bind path="query.queryLanguage">
    	<input type="hidden" name="queryLanguage" value="<c:out value="${param.ql}"/>">
    </spring:bind>
  </table>
  <input type="submit" alignment="center" value="Execute">
</form>

<%@ include file="/WEB-INF/jsp/includes/toolbar-footer.jsp" %>
