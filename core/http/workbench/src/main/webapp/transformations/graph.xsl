<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$query-result.title" />
		<xsl:text> (</xsl:text>
		<xsl:value-of select="count(//sparql:result)" />
		<xsl:text>)</xsl:text>
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

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

			function addParam(name) {
				var value = document.getElementById(name).value;
				var url = document.location.href;
				if (url.indexOf('?') + 1 || url.indexOf(';') + 1) {
					document.location.href = url + decodeURIComponent('%26') + name + '=' + encodeURIComponent(value);
				} else {
					document.location.href = url + ';' + name + '=' + encodeURIComponent(value);
				}
			}
			function textContent(element) {
				var text = element.innerText || element.textContent;
				return text.replace(/^\s*/, "").replace(/\s*$/, "");
			}
			window.onload = function() {
				var value = readCookie('Accept');
				if (value) {
					var options = document.getElementById('Accept').options;
					for (var i=0;options.length-i;i++) {
						if (options[i].value == value) {
							options[i].selected = true;
						}
					}
				}
				var count = textContent(document.getElementById('count'));
				if (document.getElementById('limit').value == '0') {
					document.getElementById('result-limited').style.display = 'none';
				} else if (parseInt(document.getElementById('limit').value) - parseInt(count)) {
					document.getElementById('result-limited').style.display = 'none';
				}
			}
			]]>
		</script>
		<pre id="count" style="display:none">
			<xsl:value-of select="count(//sparql:result)" />
		</pre>
		<form>
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of
								select="$download-format.label" />
						</th>
						<td>
							<select id="Accept" name="Accept"
								onchange="saveCookie('Accept', this.value, 30);">
								<xsl:for-each
									select="document(//sparql:link/@href)//sparql:binding[@name='download-format']">
									<option
										value="{substring-before(sparql:literal, ' ')}">
										<xsl:value-of
											select="substring-after(sparql:literal, ' ')" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td>
							<input type="submit"
								onclick="addParam('Accept');return false"
								value="{$download.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<form>
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select">
								<xsl:with-param name="onchange">addParam('limit');</xsl:with-param>
							</xsl:call-template>
						</td>
						<td id="result-limited">
							<xsl:value-of select="$result-limited.desc" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<table class="data">
			<xsl:apply-templates select="*" />
		</table>
	</xsl:template>

</xsl:stylesheet>
