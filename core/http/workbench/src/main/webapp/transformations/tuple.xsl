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
		<script type="text/javascript">
			<![CDATA[
			function addParam(name) {
				var value = document.getElementById(name).value;
				var url = document.location.href;
				if (url.indexOf('?') + 1 || url.indexOf(';') + 1) {
					document.location.href = url + decodeURIComponent('%26') + name + '=' + encodeURIComponent(value);
				} else {
					document.location.href = url + ';' + name + '=' + encodeURIComponent(value);
				}
			}
			function populateParameters() {
				var href = document.location.href;
				var elements = href.substring(href.indexOf('?') + 1).substring(href.indexOf(';') + 1).split(decodeURIComponent('%26'));
				for (var i=0;elements.length-i;i++) {
					var pair = elements[i].split('=');
					var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
					if (pair[0] == 'limit') {
						var options = document.getElementById('limit').options;
						for (var j=0;options.length-j;j++) {
							if (options[j].value == value) {
								options[j].selected = true;
							}
						}
					}
				}
			}
			function textContent(element) {
				var text = element.innerText || element.textContent;
				return text.replace(/^\s*/, "").replace(/\s*$/, "");
			}
			window.onload = function() {
				populateParameters();
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
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<select id="limit" name="limit"
								onchange="addParam('limit');">
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
