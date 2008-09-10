<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns:t="http://www.w3.org/2004/03/trix/trix-1/"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:variable name="workbench" select="//s:binding[@name = 'workbench']" />

	<xsl:variable name="download-formats"
		select="document(concat($workbench, '/../defaults/download-formats.xml'))" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$export.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId"
				select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="content">
		<xsl:variable name="Accept" select="//s:binding[@name = 'Accept']/s:literal/text()" />
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<xsl:variable name="limit" select="//s:binding[@name = 'limit']/s:literal/text()" />
		<xsl:variable name="url"
			select="concat($repository, '/statements?Accept=application/trix&amp;limit=', $limit)" />
		<xsl:variable name="data" select="document($url)" />
		<form action="{$repository}/statements">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$download-format.label" />
						</th>
						<td>
							<select id="Accept" name="Accept">
								<xsl:for-each select="$download-formats//s:result">
									<option value="{normalize-space(s:binding[@name='mime-type'])}">
										<xsl:if
											test="$Accept = s:binding[@name='mime-type']/s:literal/text()">
											<xsl:attribute name="selected">true</xsl:attribute>
										</xsl:if>
										<xsl:value-of select="s:binding[@name='name']" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td>
							<input type="submit" value="{$download.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<form action="export">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select">
								<xsl:with-param name="onchange">this.form.submit();</xsl:with-param>
							</xsl:call-template>
						</td>
						<td id="result-limited">
							<xsl:if test="number($limit) = count($data//t:triple)">
								<xsl:value-of select="$result-limited.desc" />
							</xsl:if>
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<xsl:apply-templates mode="table" select="$data" />
	</xsl:template>

</xsl:stylesheet>
