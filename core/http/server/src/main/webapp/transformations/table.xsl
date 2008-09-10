<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
   <!ENTITY rdf  "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
 ]>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns:t="http://www.w3.org/2004/03/trix/trix-1/"
	xmlns:q="http://www.openrdf.org/schema/qname#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="url-encode.xsl" />

	<xsl:template mode="table" match="s:sparql">
		<table class="data">
			<xsl:apply-templates mode="table" select="*" />
		</table>
	</xsl:template>

	<xsl:template mode="table" match="s:head">
		<thead>
			<tr>
				<xsl:apply-templates mode="table" select="s:variable" />
			</tr>
		</thead>
	</xsl:template>

	<xsl:template mode="table" match="s:variable">
		<th>
			<xsl:value-of select="@name" />
		</th>
	</xsl:template>

	<xsl:template mode="table" match="s:results">
		<tbody>
			<xsl:apply-templates mode="table" select="*" />
		</tbody>
	</xsl:template>

	<xsl:template mode="table" match="t:TriX">
		<table class="data">
			<thead>
				<tr>
					<th>
						<xsl:value-of select="$subject.label" />
					</th>
					<th>
						<xsl:value-of select="$predicate.label" />
					</th>
					<th>
						<xsl:value-of select="$object.label" />
					</th>
					<xsl:if test="t:graph/t:uri or t:graph/t:id">
						<th>
							<xsl:value-of select="$context.label" />
						</th>
					</xsl:if>
				</tr>
			</thead>
			<xsl:apply-templates mode="table" select="*" />
		</table>
	</xsl:template>

	<xsl:template mode="table" match="t:triple">
		<tr>
			<td>
				<xsl:apply-templates mode="table" select="*[1]" />
			</td>
			<td>
				<xsl:apply-templates mode="table" select="*[2]" />
			</td>
			<td>
				<xsl:apply-templates mode="table" select="*[3]" />
			</td>
			<xsl:if test="../t:uri">
				<td>
					<xsl:apply-templates mode="table" select="../t:uri" />
				</td>
			</xsl:if>
			<xsl:if test="../t:id">
				<td>
					<xsl:apply-templates mode="table" select="../t:id" />
				</td>
			</xsl:if>
		</tr>
	</xsl:template>

	<xsl:template mode="table" match="t:graph">
		<tbody>
			<xsl:apply-templates mode="table" select="t:triple" />
		</tbody>
	</xsl:template>

	<xsl:template mode="table" match="s:boolean">
		<tr>
			<td>
				<xsl:value-of select="text()" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template mode="table" match="s:result">
		<xsl:variable name="result" select="." />
		<tr>
			<xsl:for-each select="../../s:head/s:variable">
				<xsl:variable name="name" select="@name" />
				<td>
					<xsl:apply-templates mode="table" select="$result/s:binding[@name=$name]" />
				</td>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:template name="explore">
		<xsl:param name="resource" />
		<a>
			<xsl:attribute name="href">
				<xsl:text>explore?resource=</xsl:text>
				<xsl:call-template name="url-encode">
					<xsl:with-param name="str" select="$resource" />
				</xsl:call-template>
			</xsl:attribute>
			<xsl:value-of select="$resource" />
		</a>
	</xsl:template>

	<xsl:template name="explore-literal">
		<xsl:param name="literal" />
		<a>
			<xsl:attribute name="href">
				<xsl:text>explore?resource=</xsl:text>
				<xsl:choose>
					<xsl:when test="$literal/@q:qname">
						<xsl:call-template name="url-encode">
							<xsl:with-param name="str"
								select="concat('&quot;', $literal/text(), '&quot;^^', $literal/@q:qname)" />
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="url-encode">
							<xsl:with-param name="str"
								select="concat('&quot;', $literal/text(), '&quot;^^&lt;', $literal/@datatype, '&gt;')" />
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:attribute>
			<xsl:value-of select="$literal/text()" />
		</a>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;^^&lt;', @datatype, '&gt;')" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@q:qname]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="concat('&quot;', text(), '&quot;^^', @q:qname)" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;boolean']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;integer']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;decimal']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;double']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;date']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;dateTime']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;time']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&xsd;duration']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="literal" select="." />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:uri[@q:qname]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="@q:qname" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@datatype = '&rdf;XMLLiteral']">
		<pre>
			<xsl:value-of select="text()" />
		</pre>
	</xsl:template>

	<xsl:template mode="table" match="s:literal[@xml:lang]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="concat('&quot;', text(), '&quot;@', @xml:lang)" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:literal | t:plainLiteral">
		<xsl:choose>
			<xsl:when test="contains(text(), '&#10;')">
				<pre>
					<xsl:value-of select="text()" />
				</pre>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="explore">
					<xsl:with-param name="resource" select="concat('&quot;', text(), '&quot;')" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template mode="table" match="s:bnode | t:id">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="concat('_:', text())" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template mode="table" match="s:uri | t:uri">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="concat('&lt;', text(), '&gt;')" />
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
