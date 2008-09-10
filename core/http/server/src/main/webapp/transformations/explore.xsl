<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
   <!ENTITY rdf  "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
   <!ENTITY rdfs  "http://www.w3.org/2000/01/rdf-schema#" >
 ]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:s="http://www.w3.org/2005/sparql-results#"
	xmlns:t="http://www.w3.org/2004/03/trix/trix-1/" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:variable name="errors" select="//s:binding[@name = 'error-message']/s:literal/text()" />

	<xsl:variable name="resource" select="//s:binding[@name = 'resource']/s:literal/text()" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$explore.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId" select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="sort-list">
		<xsl:param name="title" />
		<xsl:param name="list" />
		<xsl:if test="$list">
			<div>
				<h3>
					<xsl:value-of select="$title" />
				</h3>
				<ul>
					<xsl:for-each select="$list">
						<xsl:sort select="." />
						<li>
							<xsl:apply-templates select="." />
						</li>
					</xsl:for-each>
				</ul>
			</div>
		</xsl:if>
	</xsl:template>

	<xsl:template name="result">
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<xsl:variable name="limit" select="//s:binding[@name = 'limit']" />
		<xsl:variable name="base"
			select="concat($repository, '/statements?Accept=application/trix&amp;limit=', $limit, '&amp;')" />
		<xsl:variable name="resource-encoded">
			<xsl:call-template name="url-encode">
				<xsl:with-param name="str" select="$resource" />
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="asObject" select="document(concat($base, 'obj=', $resource-encoded))" />
		<xsl:if test="starts-with($resource, '&lt;') or starts-with($resource, '_')">
			<xsl:variable name="asSubject" select="document(concat($base, 'subj=', $resource-encoded))" />
			<xsl:if test="1 = count($asSubject//t:triple[*[2]/text() = '&rdfs;label'])">
				<xsl:for-each select="$asSubject//t:triple[*[2]/text() = '&rdfs;label']">
					<h2>
						<xsl:value-of select="*[3]" />
					</h2>
				</xsl:for-each>
			</xsl:if>
			<xsl:if test="1 = count($asSubject//t:triple[*[2]/text() = '&rdfs;comment'])">
				<xsl:for-each select="$asSubject//t:triple[*[2]/text() = '&rdfs;comment']">
					<p>
						<xsl:value-of select="*[3]" />
					</p>
				</xsl:for-each>
			</xsl:if>
			<table class="simple">
				<tr>
					<td>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$super-classes.title" />
							<xsl:with-param name="list"
								select="$asSubject//t:triple[*[2]/text() = '&rdfs;subClassOf']/*[3]" />
						</xsl:call-template>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$sub-classes.title" />
							<xsl:with-param name="list"
								select="$asObject//t:triple[*[2]/text() = '&rdfs;subClassOf']/*[1]" />
						</xsl:call-template>
					</td>
					<td>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$properties.title" />
							<xsl:with-param name="list" select="$asObject//t:triple[*[2]/text() = '&rdfs;domain']/*[1]" />
						</xsl:call-template>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$super-properties.title" />
							<xsl:with-param name="list"
								select="$asSubject//t:triple[*[2]/text() = '&rdfs;subPropertyOf']/*[3]" />
						</xsl:call-template>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$sub-properties.title" />
							<xsl:with-param name="list"
								select="$asObject//t:triple[*[2]/text() = '&rdfs;subPropertyOf']/*[1]" />
						</xsl:call-template>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$property-domain.title" />
							<xsl:with-param name="list" select="$asSubject//t:triple[*[2]/text() = '&rdfs;domain']/*[3]" />
						</xsl:call-template>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title" select="$property-range.title" />
							<xsl:with-param name="list" select="$asSubject//t:triple[*[2]/text() = '&rdfs;range']/*[3]" />
						</xsl:call-template>
					</td>
				</tr>
			</table>
			<xsl:if test="$asSubject/t:TriX/t:graph">
				<xsl:if test="number($limit) = count($asSubject//t:triple)">
					<p>
						<xsl:value-of select="$result-limited.desc" />
					</p>
				</xsl:if>
				<xsl:apply-templates mode="table" select="$asSubject" />
			</xsl:if>
			<xsl:if test="starts-with($resource, '&lt;')">
				<xsl:variable name="asPredicate" select="document(concat($base, 'pred=', $resource-encoded))" />
				<xsl:if test="$asPredicate/t:TriX/t:graph">
					<xsl:if test="number($limit) = count($asPredicate//t:triple)">
						<p>
							<xsl:value-of select="$result-limited.desc" />
						</p>
					</xsl:if>
					<xsl:apply-templates mode="table" select="$asPredicate" />
				</xsl:if>
			</xsl:if>
		</xsl:if>
		<xsl:if test="$asObject/t:TriX/t:graph">
			<xsl:if test="number($limit) = count($asObject//t:triple)">
				<p>
					<xsl:value-of select="$result-limited.desc" />
				</p>
			</xsl:if>
			<xsl:apply-templates mode="table" select="$asObject" />
		</xsl:if>
		<xsl:if test="starts-with($resource, '&lt;') or starts-with($resource, '_')">
			<xsl:variable name="asContext" select="document(concat($base, 'context=', $resource-encoded))" />
			<xsl:if test="$asContext/t:TriX/t:graph">
				<xsl:if test="number($limit) = count($asContext//t:triple)">
					<p>
						<xsl:value-of select="$result-limited.desc" />
					</p>
				</xsl:if>
				<xsl:apply-templates mode="table" select="$asContext" />
			</xsl:if>
		</xsl:if>
	</xsl:template>

	<xsl:template name="content">
		<xsl:if test="//s:binding[@name='default-limit']/s:literal = count(//s:result)">
			<p id="result-limited">
				<xsl:value-of select="$result-limited.desc" />
			</p>
		</xsl:if>
		<xsl:if test="not($errors) and $resource">
			<xsl:call-template name="result" />
		</xsl:if>
		<form action="explore">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$resource.label" />
						</th>
						<td>
							<input id="resource" name="resource" size="48" type="text" value="{$resource}" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span class="error">
								<xsl:value-of select="//s:binding[@name='error-message']" />
							</span>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit" value="{$show.label}" />
						</td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>