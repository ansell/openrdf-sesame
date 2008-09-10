<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
 ]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:template name="html">
		<xsl:param name="title" />
		<xsl:param name="workbench" />
		<xsl:param name="server" />
		<xsl:param name="repository" />
		<xsl:param name="repositoryId" />
		<xsl:variable name="repositoryInfo"
			select="document(concat($server, '/repositories'))//s:result[s:binding[@name='id']/s:literal/text() = $repositoryId]" />
		<html xml:lang="en" lang="en">
			<head>
				<title>
					<xsl:value-of select="$workbench.title" />
					-
					<xsl:value-of select="$title" />
				</title>
				<meta name="DC.title" content="{$title}" />
				<link title="Default" rel="stylesheet" type="text/css"
					href="{$workbench}/../styles/default/print.css" media="print" />
				<link title="Default" rel="stylesheet" type="text/css"
					href="{$workbench}/../styles/default/screen.css" media="screen" />
				<xsl:comment><![CDATA[[if (gt IE 5.5)&(lt IE 7)]>
				<link title="Default" rel="stylesheet" type="text/css" href="../../styles/msie-png-alpha.css" media="screen" />
				<![endif]]]></xsl:comment>
				<xsl:comment><![CDATA[[if IE 6]>
				<link title="Default" rel="stylesheet" type="text/css" href="../../styles/default/msie-minheight.css" media="screen" />
				<![endif]]]></xsl:comment>
				<link title="Basic" rel="alternate stylesheet" type="text/css"
					href="{$workbench}/../styles/basic/all.css" media="all" />
				<link rel="shortcut icon" href="{$workbench}/../favicon.ico" type="image/ico" />
				<link rel="icon" href="{$workbench}/../favicon.png" type="image/png" />
			</head>
			<body>
				<div id="header">
					<div id="logo">
						<img src="{$workbench}/../images/logo.png" alt="" />
						<img class="productgroup" src="{$workbench}/../images/productgroup.png"
							alt="" />
					</div>
				</div>
				<div id="navigation">
					<ul class="maingroup">
						<xsl:call-template name="navigation">
							<xsl:with-param name="workbench" select="$workbench" />
							<xsl:with-param name="repositoryId" select="$repositoryId" />
							<xsl:with-param name="repositoryInfo" select="$repositoryInfo" />
						</xsl:call-template>
					</ul>
				</div>
				<div id="contentheader">
					<table>
						<tr>
							<th colspan="3">
								<xsl:value-of select="$selections.title" />
							</th>
						</tr>
						<tr>
							<th>
								<xsl:value-of select="$repository.label" />
							</th>
							<td>
								<xsl:choose>
									<xsl:when test="$repository">
										<xsl:value-of
											select="$repositoryInfo/s:binding[@name='title']" />
										(
										<xsl:value-of
											select="$repositoryInfo/s:binding[@name='id']" />
										)
									</xsl:when>
									<xsl:otherwise>
										<span class="disabled">
											<xsl:value-of select="$none.label" />
										</span>
									</xsl:otherwise>
								</xsl:choose>
							</td>
							<td class="change">
								<a href="{$workbench}">
									<xsl:value-of select="$change.label" />
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
					<xsl:call-template name="content" />
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

	<xsl:template name="navigation">
		<xsl:param name="workbench" />
		<xsl:param name="repositoryId" />
		<xsl:param name="repositoryInfo" />
		<li>
			<a>
				<xsl:choose>
					<xsl:when test="$repositoryId">
						<xsl:attribute name="href">
							<xsl:value-of
								select="concat($workbench, '/', $repositoryId, '/repositories')" />
						</xsl:attribute>
					</xsl:when>
					<xsl:otherwise>
						<xsl:attribute name="href">
							<xsl:value-of select="$workbench" />
						</xsl:attribute>
					</xsl:otherwise>
				</xsl:choose>
				<xsl:value-of select="$repository-list.label" />
			</a>
			<!-- 
			<ul class="group">
				<li>
					<a href="{$workbench}/NONE/create">
						<xsl:value-of select="$repository-create.label" />
					</a>
				</li>
				<li>
					<a href="{$workbench}/NONE/delete">
						<xsl:value-of select="$repository-delete.label" />
					</a>
				</li>
			</ul> -->
		</li>
		<li>
			<xsl:value-of select="$explore.label" />
			<ul class="group">
				<xsl:call-template name="navigation-explore">
					<xsl:with-param name="base" select="concat($workbench, '/', $repositoryId)" />
					<xsl:with-param name="disabled" select="not($repositoryId)" />
				</xsl:call-template>
			</ul>
		</li>
		<li>
			<xsl:value-of select="$modify.label" />
			<ul class="group">
				<xsl:call-template name="navigation-modify">
					<xsl:with-param name="base" select="concat($workbench, '/', $repositoryId)" />
					<xsl:with-param name="disabled"
						select="not($repositoryInfo//s:binding[@name='writable']/s:literal/text() = 'true')" />
				</xsl:call-template>
			</ul>
		</li>
		<!-- 
		<li>
			<xsl:value-of select="$system.label" />
			<ul class="group">
				<li>
					<a href="information">
						<xsl:value-of select="$information.label" />
					</a>
				</li>
			</ul>
		</li> -->
	</xsl:template>

	<xsl:template name="navigation-explore">
		<xsl:param name="base" />
		<xsl:param name="disabled" />
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$summary.label" />
			<xsl:with-param name="href" select="concat($base, '/summary')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$namespaces.label" />
			<xsl:with-param name="href" select="concat($base, '/namespaces')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$contexts.label" />
			<xsl:with-param name="href" select="concat($base, '/contexts')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$types.label" />
			<xsl:with-param name="href" select="concat($base, '/types')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$explore.label" />
			<xsl:with-param name="href" select="concat($base, '/explore')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$query.label" />
			<xsl:with-param name="href" select="concat($base, '/query')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$export.label" />
			<xsl:with-param name="href" select="concat($base, '/export')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="navigation-modify">
		<xsl:param name="base" />
		<xsl:param name="disabled" />
		<!-- 
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$add.label" />
			<xsl:with-param name="href" select="concat($base, '/add')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template> -->
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$remove.label" />
			<xsl:with-param name="href" select="concat($base, '/remove')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
		<xsl:call-template name="navigation-entry">
			<xsl:with-param name="label" select="$clear.label" />
			<xsl:with-param name="href" select="concat($base, '/clear')" />
			<xsl:with-param name="disabled" select="$disabled" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="navigation-entry">
		<xsl:param name="label" />
		<xsl:param name="href" />
		<xsl:param name="disabled" />
		<li>
			<xsl:choose>
				<xsl:when test="$disabled">
					<span class="disabled">
						<xsl:value-of select="$label" />
					</span>
				</xsl:when>
				<xsl:otherwise>
					<a href="{$href}">
						<xsl:value-of select="$label" />
					</a>
				</xsl:otherwise>
			</xsl:choose>
		</li>
	</xsl:template>

	<xsl:template name="limit-select">
		<xsl:param name="onchange" />
		<select id="limit" name="limit">
			<xsl:if test="$onchange">
				<xsl:attribute name="onchange">
					<xsl:value-of select="$onchange" />
				</xsl:attribute>
			</xsl:if>
			<xsl:variable name="limit" select="//s:binding[@name='limit']/s:literal/text()" />
			<option value="0">
				<xsl:if test="$limit = '0'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$none.label" />
			</option>
			<option value="10">
				<xsl:if test="$limit = '10'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$limit10.label" />
			</option>
			<option value="50">
				<xsl:if test="$limit = '50'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$limit50.label" />
			</option>
			<option value="100">
				<xsl:if test="$limit = '100'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$limit100.label" />
			</option>
			<option value="200">
				<xsl:if test="$limit = '200'">
					<xsl:attribute name="selected">true</xsl:attribute>
				</xsl:if>
				<xsl:value-of select="$limit200.label" />
			</option>
		</select>
	</xsl:template>

</xsl:stylesheet>
