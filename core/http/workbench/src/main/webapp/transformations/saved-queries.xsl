<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title" select="$saved-queries.title" />

	<xsl:include href="template.xsl" />

	<xsl:variable name="user"
		select="normalize-space($info//sparql:binding[@name='server-user'])" />

	<xsl:template match="sparql:sparql"></xsl:template>
</xsl:stylesheet>
