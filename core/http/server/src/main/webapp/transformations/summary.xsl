<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$summary.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId"
				select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="content">
		<xsl:variable name="server" select="//s:binding[@name = 'server']" />
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<xsl:variable name="repositoryId"
			select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		<xsl:variable name="repositoryInfo"
			select="document(concat($server, '/repositories'))//s:result[s:binding[@name='id']/s:literal/text() = $repositoryId]" />
		<xsl:variable name="sizeInfo" select="document(concat($repository, '/size'))" />
		<xsl:variable name="namespaceInfo" select="document(concat($repository, '/namespaces'))" />
		<xsl:variable name="contextInfo" select="document(concat($repository, '/contexts'))" />
		<h2>
			<xsl:value-of select="$repository-location.title" />
		</h2>
		<table class="simple">
			<tbody>
				<tr>
					<th>
						<xsl:value-of select="$repository-id.label" />
					</th>
					<td>
						<xsl:value-of select="$repositoryInfo//s:binding[@name='id']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$repository-title.label" />
					</th>
					<td>
						<xsl:value-of select="$repositoryInfo//s:binding[@name='title']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$repository-location.label" />
					</th>
					<td>
						<xsl:value-of select="$repositoryInfo//s:binding[@name='uri']" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$server.label" />
					</th>
					<td>
						<xsl:value-of select="$server" />
					</td>
				</tr>
			</tbody>
		</table>

		<h2>
			<xsl:value-of select="$repository-size.title" />
		</h2>
		<table class="simple">
			<tbody>
				<tr>
					<th>
						<xsl:value-of select="$repository-size.label" />
					</th>
					<td>
						<xsl:value-of select="$sizeInfo" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$number-of-namespaces.label" />
					</th>
					<td>
						<xsl:value-of select="count($namespaceInfo//s:result)" />
					</td>
				</tr>
				<tr>
					<th>
						<xsl:value-of select="$number-of-contexts.label" />
					</th>
					<td>
						<xsl:value-of select="count($contextInfo//s:result)" />
					</td>
				</tr>
			</tbody>
		</table>
	</xsl:template>

</xsl:stylesheet>
