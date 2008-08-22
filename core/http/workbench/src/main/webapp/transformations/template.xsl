<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:template match="/">
		<xsl:variable name="info"
			select="document(sparql:sparql/sparql:head/sparql:link[@href='info']/@href)" />
		<html xml:lang="en" lang="en">
			<head>
				<title>
					<xsl:value-of select="$workbench.title" />
					-
					<xsl:value-of select="$title" />
				</title>
				<meta name="DC.title" content="{$title}" />
				<link title="Default" rel="stylesheet" type="text/css"
					href="../../styles/default/print.css" media="print" />
				<link title="Default" rel="stylesheet" type="text/css"
					href="../../styles/default/screen.css" media="screen" />
				<xsl:comment><![CDATA[[if (gt IE 5.5)&(lt IE 7)]>
				<link title="Default" rel="stylesheet" type="text/css" href="../../styles/msie-png-alpha.css" media="screen" />
				<![endif]]]></xsl:comment>
				<xsl:comment><![CDATA[[if IE 6]>
				<link title="Default" rel="stylesheet" type="text/css" href="../../styles/default/msie-minheight.css" media="screen" />
				<![endif]]]></xsl:comment>
				<link title="Basic" rel="alternate stylesheet"
					type="text/css" href="../../styles/basic/all.css" media="all" />
				<link rel="shortcut icon" href="../../favicon.ico"
					type="image/ico" />
				<link rel="icon" href="../../favicon.png"
					type="image/png" />
			</head>
			<body>
				<div id="header">
					<div id="logo">
						<img src="../../images/logo.png" alt="" />
						<img class="productgroup"
							src="../../images/productgroup.png" alt="" />
					</div>
				</div>
				<div id="navigation">
					<ul class="maingroup">
						<li>
							<a href="../NONE/server">
								<xsl:value-of select="$server.label" />
							</a>
						</li>
						<li>
							<a href="repositories">
								<xsl:value-of
									select="$repository-list.label" />
							</a>
							<ul class="group">
								<li>
									<a href="create">
										<xsl:value-of
											select="$repository-create.label" />
									</a>
								</li>
								<li>
									<a href="delete">
										<xsl:value-of
											select="$repository-delete.label" />
									</a>
								</li>
							</ul>
						</li>
						<xsl:if
							test="$info//sparql:binding[@name='readable']/sparql:literal/text() != 'false'">
							<li>
								<xsl:value-of select="$explore.label" />
								<ul class="group">
									<li>
										<a href="namespaces">
											<xsl:value-of
												select="$namespaces.label" />
										</a>
									</li>
									<li>
										<a href="contexts">
											<xsl:value-of
												select="$contexts.label" />
										</a>
									</li>
									<li>
										<a href="types">
											<xsl:value-of
												select="$types.label" />
										</a>
									</li>
									<li>
										<a href="explore">
											<xsl:value-of
												select="$explore.label" />
										</a>
									</li>
									<li>
										<a href="query">
											<xsl:value-of
												select="$query.label" />
										</a>
									</li>
									<li>
										<a href="export">
											<xsl:value-of
												select="$export.label" />
										</a>
									</li>
								</ul>
							</li>
						</xsl:if>
						<xsl:if test="$info//sparql:binding[@name='writeable']/sparql:literal/text() != 'false'">
							<li>
								<xsl:value-of select="$modify.label" />
								<ul class="group">
									<li>
										<a href="add">
											<xsl:value-of
												select="$add.label" />
										</a>
									</li>
									<li>
										<a href="remove">
											<xsl:value-of
												select="$remove.label" />
										</a>
									</li>
									<li>
										<a href="clear">
											<xsl:value-of
												select="$clear.label" />
										</a>
									</li>
								</ul>
							</li>
						</xsl:if>
						<li>
							<xsl:value-of select="$system.label" />
							<ul class="group">
								<li>
									<a href="information">
										<xsl:value-of
											select="$information.label" />
									</a>
								</li>
							</ul>
						</li>
					</ul>
				</div>
				<div id="contentheader">
					<table>
						<tr>
							<th colspan="3">
								<xsl:value-of
									select="$selections.title" />
							</th>
						</tr>
						<tr>
							<th>
								<xsl:value-of select="$server.label" />
							</th>
							<td>
								<xsl:choose>
									<xsl:when test="$info">
										<xsl:value-of
											select="$info//sparql:binding[@name='server']/sparql:literal" />
									</xsl:when>
									<xsl:otherwise>
										<span class="disabled">
											<xsl:value-of
												select="$none.label" />
										</span>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="change">
								<a href="../NONE/server">
									<xsl:value-of
										select="$change.label" />
								</a>
							</td>
						</tr>
						<tr>
							<th>
								<xsl:value-of
									select="$repository.label" />
							</th>
							<td>
								<xsl:choose>
									<xsl:when
										test="$info//sparql:binding[@name='id']">
										<xsl:value-of
											select="$info//sparql:binding[@name='description']/sparql:literal" />
										(
										<xsl:value-of
											select="$info//sparql:binding[@name='id']/sparql:literal" />
										)
									</xsl:when>
									<xsl:otherwise>
										<span class="disabled">
											<xsl:value-of
												select="$none.label" />
										</span>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="change">
								<a href="../NONE/repositories">
									<xsl:value-of
										select="$change.label" />
								</a>
							</td>
						</tr>
						<tr>
							<td colspan="3"></td>
						</tr>
					</table>
				</div>
				<div id="content">
					<h1>
						<xsl:value-of select="$title" />
					</h1>
					<xsl:apply-templates select="*" />
				</div>
				<div id="footer">
					<p>
						<div>
							<xsl:value-of select="$copyright.label" />
						</div>
						<a href="http://www.aduna-software.com/">
							<xsl:value-of select="$aduna.label" />
						</a>
					</p>
				</div>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
