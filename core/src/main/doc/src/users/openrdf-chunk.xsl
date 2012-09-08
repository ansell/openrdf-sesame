<?xml version='1.0'?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version='1.0'
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                exclude-result-prefixes="#default">

<xsl:import href="../docbook-xsl-1.68.1/html/chunk.xsl"/>

<xsl:variable name="toc.section.depth">3</xsl:variable>
<xsl:variable name="toc.max.depth">3</xsl:variable>
<xsl:variable name="label.from.part">1</xsl:variable>
<xsl:variable name="section.label.includes.component.label">1</xsl:variable>
<xsl:variable name="section.autolabel">1</xsl:variable>
<xsl:variable name="section.inherit.numeration">3</xsl:variable>
<xsl:variable name="html.stylesheet" select="'openrdf.css'"/>
<!-- Add other variable definitions here -->

<xsl:template match="section[@role = 'NotInToc']"  mode="toc" />

</xsl:stylesheet>
