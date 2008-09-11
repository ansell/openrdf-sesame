<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns:t="http://www.w3.org/2004/03/trix/trix-1/"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:variable name="workbench" select="//s:binding[@name = 'workbench']"/>

	<xsl:variable name="repository" select="//s:binding[@name = 'repository']"/>

	<xsl:variable name="query-languages" select="document(concat($workbench, '/../defaults/query-languages.xml'))"/>

	<xsl:variable name="namespaces" select="document(concat($repository, '/namespaces'))"/>

	<xsl:variable name="queryLn" select="//s:binding[@name = 'queryLn']/s:literal/text()" />
	<xsl:variable name="query" select="//s:binding[@name = 'query']/s:literal/text()" />
	<xsl:variable name="limit" select="//s:binding[@name = 'limit']/s:literal/text()" />
	<xsl:variable name="infer" select="//s:binding[@name = 'infer']/s:literal/text()" />
	<xsl:variable name="errors" select="//s:binding[@name = 'error-message']" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$query.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId" select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template name="content">
		<xsl:variable name="query-type" select="//s:binding[@name = 'query-type']/s:literal/text()" />
		<script type="text/javascript">
			<![CDATA[
			function textContent(element) {
				var text = element.innerText || element.textContent;
				return text.replace(/^\s*/, "").replace(/\s*$/, "");
			}
			var currentQueryLn;
			function loadNamespaces() {
				var query = document.getElementById('query');
				var queryLn = document.getElementById('queryLn').value;
				var namespaces = document.getElementById(queryLn + '-namespaces');
				var last = document.getElementById(currentQueryLn + '-namespaces');
				if (namespaces) {
					if (!query.value) {
						query.value = namespaces.innerText || namespaces.textContent;
						currentQueryLn = queryLn;
					}
					if (last) {
						var text = last.innerText || last.textContent;
						if (query.value == text) {
							query.value = namespaces.innerText || namespaces.textContent;
							currentQueryLn = queryLn;
						}
					}
				}
			}
			window.onload = function() {
				loadNamespaces();
			}
			function addParam(sb, name, id) {
				if (!id) {
					id = name;
				}
				var tag = document.getElementById(id);
				sb[sb.length] = name;
				sb[sb.length] = '=';
				if (tag.type == "checkbox") {
					if (tag.checked) {
						sb[sb.length] = 'true';
					} else {
						sb[sb.length] = 'false';
					}
				} else {
					sb[sb.length] = encodeURIComponent(tag.value);
				}
				sb[sb.length] = '&';
			}
			/* MSIE6 does not like xslt w/ this querystring, so we use url parameters. */
			function doSubmit() {
				var url = [];
				url[url.length] = 'query';
				if (document.all) {
					url[url.length] = ';';
				} else {
					url[url.length] = '?';
				}
				addParam(url, 'queryLn');
				addParam(url, 'query');
				addParam(url, 'limit');
				addParam(url, 'infer');
				url[url.length - 1] = '';
				document.location.href = url.join('');
				return false;
				return true;
			}
			]]>
		</script>
		<xsl:choose>
			<xsl:when test="not($query) or not($queryLn) or $errors">
				<xsl:call-template name="form"/>
			</xsl:when>
			<xsl:when test="$query-type = 'boolean'">
				<xsl:call-template name="result-boolean">
					<xsl:with-param name="mime-type">application/sparql-results+xml</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$query-type = 'graph'">
				<xsl:call-template name="result-table">
					<xsl:with-param name="mime-type">application/trix</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$query-type = 'tuple'">
				<xsl:call-template name="result-table">
					<xsl:with-param name="mime-type">application/sparql-results+xml</xsl:with-param>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="result-table"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="result-table">
		<xsl:param name="mime-type"/>
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<xsl:variable name="queryString" select="//s:binding[@name = 'queryString']" />
		<xsl:variable name="url">
			<xsl:value-of select="concat($repository, '?')"/>
			<xsl:if test="$mime-type">
				<xsl:text>Accept=</xsl:text>
				<xsl:call-template name="url-encode">
					<xsl:with-param name="str" select="$mime-type"/>
				</xsl:call-template>
				<xsl:text>&amp;</xsl:text>
			</xsl:if>
			<xsl:value-of select="$queryString"/>
		</xsl:variable>
		<xsl:variable name="data" select="document($url)" />

		<form action="query" onsubmit="return doSubmit()">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<input type="hidden" id="queryLn" name="queryLn" value="{normalize-space($queryLn)}" />
							<input type="hidden" id="query" name="query" value="{$query}" />
							<input type="hidden" id="infer" name="infer" value="{normalize-space($infer)}"/>
							<xsl:call-template name="limit-select">
								<xsl:with-param name="onchange" select="'doSubmit()'"/>
							</xsl:call-template>
						</td>
						<td>
							<xsl:if test="number($limit) = count($data//t:triple) or number($limit) = count($data//s:result)">
								<xsl:value-of select="$result-limited.desc" />
							</xsl:if>
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<xsl:apply-templates mode="table" select="$data" />
	</xsl:template>

	<xsl:template name="result-boolean">
		<xsl:param name="mime-type"/>
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<xsl:variable name="queryString" select="//s:binding[@name = 'queryString']" />
		<xsl:variable name="url">
			<xsl:value-of select="concat($repository, '?')"/>
			<xsl:if test="$mime-type">
				<xsl:text>Accept=</xsl:text>
				<xsl:call-template name="url-encode">
					<xsl:with-param name="str" select="$mime-type"/>
				</xsl:call-template>
				<xsl:text>&amp;</xsl:text>
			</xsl:if>
			<xsl:value-of select="$queryString"/>
		</xsl:variable>
		<xsl:apply-templates mode="boolean" select="document($url)" />
	</xsl:template>

	<xsl:template mode="boolean" match="s:boolean">
		<div class="queryResult">
			<xsl:choose>
				<xsl:when test="text() = 'true'">
					<img src="../../images/affirmative.png"
						alt="{$true.label}" title="{$true.label}" />
					<xsl:value-of select="$true.label" />
				</xsl:when>
				<xsl:otherwise>
					<img src="../../images/negative.png"
						alt="{$false.label}" title="{$false.label}" />
					<xsl:value-of select="$false.label" />
				</xsl:otherwise>
			</xsl:choose>
		</div>
	</xsl:template>

	<xsl:template name="form">
		<form action="query" onsubmit="return doSubmit()">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$query-language.label" />
						</th>
						<td>
							<select id="queryLn" name="queryLn" onchange="loadNamespaces()">
								<xsl:for-each select="$query-languages//s:result">
									<option value="{normalize-space(s:binding[@name='id'])}">
										<xsl:if
											test="$queryLn = s:binding[@name='id']/s:literal/text()">
											<xsl:attribute name="selected">true</xsl:attribute>
										</xsl:if>
										<xsl:value-of select="s:binding[@name='name']" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$query-string.label" />
						</th>
						<td>
							<textarea id="query" name="query" rows="16" cols="80">
								<xsl:value-of select="$query" />
							</textarea>
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span id="queryString.errors" class="error">
								<xsl:value-of select="$errors" />
							</span>
						</td>
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
							<input id="infer" name="infer" type="checkbox" value="true">
								<xsl:if
									test="$infer = 'true'">
									<xsl:attribute name="checked">true</xsl:attribute>
								</xsl:if>
							</input>
							<xsl:value-of select="$include-inferred.label" />
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>
						<td colspan="2">
							<input type="submit" value="{$execute.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<pre id="SPARQL-namespaces" style="display:none">
			<xsl:for-each
				select="$namespaces//s:results/s:result">
				<xsl:value-of
					select="concat('PREFIX ', s:binding[@name='prefix']/s:literal, ':&lt;', s:binding[@name='namespace']/s:literal, '&gt;')" />
				<xsl:text>
</xsl:text>
			</xsl:for-each>
		</pre>
		<pre id="SeRQL-namespaces" style="display:none">
			<xsl:text>
USING NAMESPACE</xsl:text>
			<xsl:for-each
				select="$namespaces//s:results/s:result">
				<xsl:text>
</xsl:text>
				<xsl:choose>
					<xsl:when test="following-sibling::s:result">
						<xsl:value-of
							select="concat(s:binding[@name='prefix']/s:literal, ' = &lt;', s:binding[@name='namespace']/s:literal, '&gt;,')" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="concat(s:binding[@name='prefix']/s:literal, ' = &lt;', s:binding[@name='namespace']/s:literal, '&gt;')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</pre>
	</xsl:template>

</xsl:stylesheet>
