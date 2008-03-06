<%@ include file="/WEB-INF/jsp/includes/toolbar-header.jsp" %>

<p>Query result:</p>
<pre class="queryResult">
<c:out value="${result}" escapeXml="true"/>
</pre>

<%@ include file="/WEB-INF/jsp/includes/toolbar-footer.jsp" %>
