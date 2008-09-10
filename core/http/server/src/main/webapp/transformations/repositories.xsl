<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
 ]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:variable name="workbench" select="//s:binding[@name = 'workbench']" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$repository-list.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId" select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="content">
		<xsl:variable name="server" select="//s:binding[@name = 'server']" />
		<xsl:variable name="url" select="concat($server, '/repositories')" />
		<xsl:apply-templates mode="table" select="document($url)/s:sparql" />
	</xsl:template>

	<xsl:template mode="table" match="s:head">
		<thead>
			<tr>
				<th>
					<img src="{$workbench}/../images/view.png" alt="{$readable.label}"
						title="{$readable.label}" />
				</th>
				<th>
					<img src="{$workbench}/../images/edit.png" alt="{$writeable.label}"
						title="{$writeable.label}" />
				</th>
				<th>
					<xsl:value-of select="$repository-id.label" />
				</th>
				<th>
					<xsl:value-of select="$repository-title.label" />
				</th>
				<th>
					<xsl:value-of select="$repository-location.label" />
				</th>
			</tr>
		</thead>
	</xsl:template>

	<xsl:template mode="table" match="s:result">
		<tr>
			<td>
				<xsl:apply-templates mode="table" select="s:binding[@name='readable']" />
			</td>
			<td>
				<xsl:apply-templates mode="table" select="s:binding[@name='writable']" />
			</td>
			<td>
				<xsl:apply-templates mode="table" select="s:binding[@name='id']" />
			</td>
			<td>
				<xsl:apply-templates mode="table" select="s:binding[@name='title']" />
			</td>
			<td>
				<xsl:apply-templates mode="table" select="s:binding[@name='uri']" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template mode="table" match="s:binding[@name='id']">
		<a href="{$workbench}/{s:literal}">
			<xsl:value-of select="s:literal" />
		</a>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;boolean']">
		<xsl:choose>
			<xsl:when test="text() = 'true'">
				<img src="{$workbench}/../images/affirmative.png" alt="{$true.label}"
					title="{$true.label}" />
			</xsl:when>
			<xsl:otherwise>
				<img src="{$workbench}/../images/negative.png" alt="{$false.label}"
					title="{$false.label}" />
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="table" match="s:literal">
		<xsl:value-of select="text()" />
	</xsl:template>

	<xsl:template mode="table" match="s:uri">
		<xsl:value-of select="text()" />
	</xsl:template>

</xsl:stylesheet>
