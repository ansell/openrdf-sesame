<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$contexts.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId"
				select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="content">
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<xsl:variable name="url" select="concat($repository, '/contexts')" />
		<xsl:apply-templates mode="table" select="document($url)" />
	</xsl:template>

</xsl:stylesheet>
