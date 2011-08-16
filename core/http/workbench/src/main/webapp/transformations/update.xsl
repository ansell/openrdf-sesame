<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$update.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<script type="text/javascript">
			<![CDATA[
			function populateParameters() {
				var href = document.location.href;
				var elements = href.substring(href.indexOf('?') + 1).substring(href.indexOf(';') + 1).split(decodeURIComponent('%26'));
				for (var i=0;elements.length-i;i++) {
					var pair = elements[i].split('=');
					var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
					var q = document.getElementById('update');
					if (pair[0] == 'update') if (!q.value) {
							q.value = value;
					}
				}
			}
			function textContent(element) {
				var text = element.innerText || element.textContent;
				return text.replace(/^\s*/, "").replace(/\s*$/, "");
			}
			var currentqueryLn;
			function loadNamespaces() {
				var update = document.getElementById('update');
				var queryLn = document.getElementById('queryLn').value;
				var namespaces = document.getElementById(queryLn + '-namespaces');
				var last = document.getElementById(currentQueryLn + '-namespaces');
				if (namespaces) {
					if (!update.value) {
						update.value = namespaces.innerText || namespaces.textContent;
						currentqueryLn = queryLn;
					}
					if (last) {
						var text = last.innerText || last.textContent;
						if (update.value == text) {
							update.value = namespaces.innerText || namespaces.textContent;
							currentqueryLn = queryLn;
						}
					}
				}
			}
			window.onload = function() {
				populateParameters();
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
			/* MSIE6 does not like xslt w/ this updatestring, so we use url parameters. */
			function doSubmit() {
				if (document.getElementById('update').value.length >= 1000) {
					// some functionality will not work as expected on result pages
					return true;
				} else { // safe to use in request-URI
					var url = [];
					url[url.length] = 'update';
					if (document.all) {
						url[url.length] = ';';
					} else {
						url[url.length] = '?';
					}
					addParam(url, 'queryLn');
					addParam(url, 'update');
					addParam(url, 'limit');
					addParam(url, 'infer');
					url[url.length - 1] = '';
					document.location.href = url.join('');
					return false;
				}
			}
			]]>
		</script>
		<form action="update" method="POST" onsubmit="return doSubmit()">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$query-language.label" />
						</th>
						<td>
							<select id="queryLn" name="queryLn" onchange="loadNamespaces()">
								<option value="SPARQL" selected="true">
									SPARQL
								</option>
<!-- 
								<xsl:for-each select="$info//sparql:binding[@name='query-format']">
									<option value="{substring-before(sparql:literal, ' ')}">
										<xsl:if
											test="$info//sparql:binding[@name='default-queryLn']/sparql:literal = substring-before(sparql:literal, ' ')">
											<xsl:attribute name="selected">true</xsl:attribute>
										</xsl:if>
										<xsl:value-of select="substring-after(sparql:literal, ' ')" />
									</option>
								</xsl:for-each>
-->
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$update-string.label" />
						</th>
						<td>
							<textarea id="update" name="update" rows="16" cols="80">
							</textarea>
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span id="updateString.errors" class="error">
								<xsl:value-of select="//sparql:binding[@name='error-message']" />
							</span>
						</td>
					</tr>
					<!-- <tr> <td></td> <td> <input id="infer" name="infer" type="checkbox" 
						value="true"> <xsl:if test="$info//sparql:binding[@name='default-infer']/sparql:literal 
						= 'true'"> <xsl:attribute name="checked">true</xsl:attribute> </xsl:if> </input> 
						<xsl:value-of select="$include-inferred.label" /> </td> <td></td> </tr> -->
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
				select="document(//sparql:link[@href='namespaces']/@href)//sparql:results/sparql:result">
				<xsl:value-of
					select="concat('PREFIX ', sparql:binding[@name='prefix']/sparql:literal, ':&lt;', sparql:binding[@name='namespace']/sparql:literal, '&gt;')" />
				<xsl:text>
</xsl:text>
			</xsl:for-each>
		</pre>
	</xsl:template>

</xsl:stylesheet>
