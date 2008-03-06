<%@ include file="/WEB-INF/jsp/includes/headers.jsp" %>

<html>
<head>
  <title><fmt:message key="title"/></title>
  <link rel="stylesheet" href="styles/default/screen.css" type="text/css">
</head>
<body>
<h1><fmt:message key="repository.heading"/>: <c:out value="${param.descr}"/></h1>
<p>
[<a href="<c:url value="${param.repository}"/>"><c:out value="${param.repository}"/></a>]
</p>
<p>
[
<a href="<c:url value="select.view?ql=SERQL&repository=${param.repository}&descr=${param.descr}"/>"><fmt:message key="repository.action.serql-select"/></a>
|
<a href="<c:url value="construct.view?ql=SERQL&repository=${param.repository}&descr=${param.descr}"/>"><fmt:message key="repository.action.serql-construct"/></a>
|
<a href="<c:url value="select.view?ql=SPARQL&repository=${param.repository}&descr=${param.descr}"/>"><fmt:message key="repository.action.sparql-select"/></a>
|
<a href="<c:url value="construct.view?ql=SPARQL&repository=${param.repository}&descr=${param.descr}"/>"><fmt:message key="repository.action.sparql-construct"/></a>
]
</p>
