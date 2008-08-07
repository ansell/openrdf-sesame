<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
   <!ENTITY rdf  "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns:q="http://www.openrdf.org/schema/qname#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="url-encode.xsl" />

	<xsl:template match="sparql:sparql">
		<table class="data">
			<xsl:apply-templates select="*" />
		</table>
	</xsl:template>

	<xsl:template match="sparql:head">
		<thead>
			<tr>
				<xsl:apply-templates select="sparql:variable" />
			</tr>
		</thead>
	</xsl:template>

	<xsl:template match="sparql:variable[@name='readable']">
		<th>
			<img src="/openrdf-workbench/images/view.png" alt="Readable"
				title="Readable" />
		</th>
	</xsl:template>

	<xsl:template match="sparql:variable[@name='writeable']">
		<th>
			<img src="/openrdf-workbench/images/edit.png"
				alt="writeable" title="writeable" />
		</th>
	</xsl:template>

	<xsl:template match="sparql:variable">
		<th>
			<xsl:value-of select="@name" />
		</th>
	</xsl:template>

	<xsl:template match="sparql:results">
		<tbody>
			<xsl:apply-templates select="*" />
		</tbody>
	</xsl:template>

	<xsl:template match="sparql:boolean">
		<tr>
			<td>
				<xsl:value-of select="text()" />
			</td>
		</tr>
	</xsl:template>

	<xsl:template match="sparql:result">
		<xsl:variable name="result" select="." />
		<tr>
			<xsl:for-each select="../../sparql:head/sparql:variable">
				<xsl:variable name="name" select="@name" />
				<td>
					<xsl:apply-templates
						select="$result/sparql:binding[@name=$name]" />
				</td>
			</xsl:for-each>
		</tr>
	</xsl:template>

	<xsl:template name="explore">
		<xsl:param name="resource" />
		<xsl:variable name="uri">
			<xsl:call-template name="url-encode">
				<xsl:with-param name="str" select="$resource" />
			</xsl:call-template>
		</xsl:variable>
		<a href="explore?resource={$uri}">
			<xsl:value-of select="$resource" />
		</a>
	</xsl:template>

	<xsl:template name="explore-literal">
		<xsl:param name="label" />
		<xsl:param name="datatype" />
		<xsl:variable name="uri">
			<xsl:call-template name="url-encode">
				<xsl:with-param name="str"
					select="concat('&quot;', $label, '&quot;^^&lt;', $datatype, '&gt;')" />
			</xsl:call-template>
		</xsl:variable>
		<a href="explore?resource={$uri}">
			<xsl:value-of select="$label" />
		</a>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;^^&lt;', @datatype, '&gt;')" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@q:qname]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;^^', @q:qname)" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;boolean']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;integer']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;decimal']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;double']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;date']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;dateTime']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;time']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal[@datatype = '&xsd;duration']">
		<xsl:call-template name="explore-literal">
			<xsl:with-param name="label" select="text()" />
			<xsl:with-param name="datatype" select="@datatype" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:uri[@q:qname]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource" select="@q:qname" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template
		match="sparql:literal[@datatype = '&rdf;XMLLiteral']">
		<pre>
			<xsl:value-of select="text()" />
		</pre>
	</xsl:template>

	<xsl:template match="sparql:literal[@xml:lang]">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&quot;', text(), '&quot;@', @xml:lang)" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:literal">
		<xsl:choose>
			<xsl:when test="contains(text(), '&#10;')">
				<pre>
					<xsl:value-of select="text()" />
				</pre>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="explore">
					<xsl:with-param name="resource"
						select="concat('&quot;', text(), '&quot;')" />
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="sparql:bnode">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('_:', text())" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="sparql:uri">
		<xsl:call-template name="explore">
			<xsl:with-param name="resource"
				select="concat('&lt;', text(), '&gt;')" />
		</xsl:call-template>
	</xsl:template>

</xsl:stylesheet>
