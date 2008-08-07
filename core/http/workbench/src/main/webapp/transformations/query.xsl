<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$query.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<iframe id="cookie-iframe" name="cookie-iframe"
			style="visibility:hidden;position:absolute;" width="0" height="0"
			src="../../scripts/cookies.html">
		</iframe>
		<script type="text/javascript">
			<![CDATA[
			function saveCookie(name,value,days) {
				if (days) {
					var date = new Date();
					date.setTime(date.getTime()+(days*24*60*60*1000));
					var expires = "; expires="+date.toGMTString();
				}
				else var expires = "";
				getCookieDocument().cookie = name+"="+value+expires+"; path=/";
			}
			
			function readCookie(name) {
				if (!getCookieDocument().cookie)
					return null;
				var nameEQ = name + "=";
				var ca = getCookieDocument().cookie.split(';');
				for(var i=0;ca.length - i;i++) {
					var c = ca[i];
					while (c.charAt(0)==' ') c = c.substring(1,c.length);
					if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
				}
				return null;
			}
			
			function getCookieDocument() {
				if (window.frames['cookie-iframe'])
					return window.frames['cookie-iframe'].document;
				return document;
			}

			function populateParameters() {
				var href = document.location.href;
				var elements = href.substring(href.indexOf('?') + 1).substring(href.indexOf(';') + 1).split(decodeURIComponent('%26'));
				for (var i=0;elements.length-i;i++) {
					var pair = elements[i].split('=');
					var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
					var q = document.getElementById('query');
					if (pair[0] == 'query') if (!q.value) {
							q.value = value;
					}
				}
			}
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
				if (namespaces) if (!query.value) {
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
			window.onload = function() {
				populateParameters();
				var value = readCookie('queryLn');
				if (value) {
					var options = document.getElementById('queryLn').options;
					for (var i=0;options.length-i;i++) {
						if (options[i].value == value) {
							options[i].selected = true;
						}
					}
				}
				value = readCookie('infer');
				if (value) {
					var box = document.getElementById('infer');
					if ("true" == value) {
						box.checked = true;
					} else if ("false" == value) {
						box.checked = false;
					}
				}
				value = readCookie('limit');
				if (value) {
					var options = document.getElementById('limit').options;
					for (var i=0;options.length-i;i++) {
						if (options[i].value == value) {
							options[i].selected = true;
						}
					}
				}
				loadNamespaces();
			}
			function addParam(sb, name, id) {
				if (!id) {
					id = name;
				}
				sb[sb.length] = name;
				sb[sb.length] = '=';
				sb[sb.length] = encodeURIComponent(document.getElementById(id).value);
				sb[sb.length] = '&';
			}
			/* MSIE does not like xslt w/ this querystring, so we use url parameters. */
			function doSubmit() {
				var url = [];
				url[url.length] = 'query';
				url[url.length] = ';';
				addParam(url, 'queryLn');
				addParam(url, 'query');
				addParam(url, 'limit');
				addParam(url, 'infer');
				url[url.length - 1] = '';
				document.location.href = url.join('');
			}
			]]>
		</script>
		<form action="query" onsubmit="doSubmit(); return false">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of
								select="$query-language.label" />
						</th>
						<td>
							<select id="queryLn" name="queryLn"
								onchange="loadNamespaces();saveCookie('queryLn', this.value, 30)">
								<xsl:for-each
									select="document(//sparql:link/@href)//sparql:binding[@name='query-format']">
									<option
										value="{substring-before(sparql:literal, ' ')}">
										<xsl:value-of
											select="substring-after(sparql:literal, ' ')" />
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
							<textarea id="query" name="query" rows="6"
								cols="70">
							</textarea>
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span id="queryString.errors"
								class="error">
								<xsl:value-of
									select="//sparql:binding[@name='error-message']" />
							</span>
						</td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<select id="limit" name="limit"
								onchange="saveCookie('limit', this.value, 30)">
								<option value="0">
									<xsl:value-of select="$none.label" />
								</option>
								<option value="10">
									<xsl:value-of
										select="$limit10.label" />
								</option>
								<option value="50">
									<xsl:value-of
										select="$limit50.label" />
								</option>
								<option value="100" selected="true">
									<xsl:value-of
										select="$limit100.label" />
								</option>
								<option value="200">
									<xsl:value-of
										select="$limit200.label" />
								</option>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input id="infer" name="infer"
								type="checkbox" value="true" checked="true"
								onchange="saveCookie('infer', this.checked, 30)" />
							<xsl:value-of
								select="$include-inferred.label" />
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>
						<td colspan="2">
							<input type="submit"
								value="{$execute.label}" />
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
		<pre id="SeRQL-namespaces" style="display:none">
			<xsl:text>
USING NAMESPACE</xsl:text>
			<xsl:for-each
				select="document(//sparql:link[@href='namespaces']/@href)//sparql:results/sparql:result">
				<xsl:text>
</xsl:text>
				<xsl:choose>
					<xsl:when test="following-sibling::sparql:result">
						<xsl:value-of
							select="concat(sparql:binding[@name='prefix']/sparql:literal, ' = &lt;', sparql:binding[@name='namespace']/sparql:literal, '&gt;,')" />
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of
							select="concat(sparql:binding[@name='prefix']/sparql:literal, ' = &lt;', sparql:binding[@name='namespace']/sparql:literal, '&gt;')" />
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</pre>
	</xsl:template>

</xsl:stylesheet>
