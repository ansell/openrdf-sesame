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
	
	<xsl:variable name="nextX.label">
		<xsl:value-of select="$next.label" />
		<xsl:text> </xsl:text>
		<xsl:value-of select="count(//sparql:result)" />
	</xsl:variable>

	<xsl:variable name="previousX.label">
		<xsl:value-of select="$previous.label" />
		<xsl:text> </xsl:text>
		<xsl:value-of select="count(//sparql:result)" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template match="sparql:sparql">
		<script type="text/javascript">
			<![CDATA[
			function addLimit() {
				addParam('limit', document.getElementById('limit').value);
			}
			
			function addParam(name, value) {
				var url = document.location.href;
				var anyParams = (url.indexOf('?') + 1 || url.indexOf(';') + 1);
				var sep =  anyParams ? decodeURIComponent('%26') : ';';
				
				// 'value' is always a decimal integer
				document.location.href = url + sep + name + '=' + value;
			}
			
			function nextOffset() {
			    var limit = document.getElementById('limit').value;
			    var offset = getLastOffset() + parseInt(limit, 10);
			    addParam('offset', offset);
			}

			function previousOffset() {
			    var limit = document.getElementById('limit').value;
			    var offset = getLastOffset() - parseInt(limit, 10);
			    addParam('offset', offset);
			}
			
			function getLastOffset() {
				var href = document.location.href;
				var elements = href.substring(href.indexOf('?') + 1).substring(href.indexOf(';') + 1).split(decodeURIComponent('%26'));
				var offset = 0;
				for (var i=0;elements.length-i;i++) {
					var pair = elements[i].split('=');
					if (pair[0] != 'offset') {
						continue;
					}
					offset = parseInt(pair[1], 10);
				}
				return offset;
			}
			]]>
		</script>
		<form>
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select">
								<xsl:with-param name="onchange">addLimit();</xsl:with-param>
							</xsl:call-template>
						</td>
						<td id="result-limited">
							<xsl:if
								test="$info//sparql:binding[@name='default-limit']/sparql:literal = count(//sparql:result)">
								<xsl:value-of
									select="$result-limited.desc" />
							</xsl:if>
						</td>
					</tr>
					<tr>
					    <th />
						<td>
							<input type="button"
								value="{$previousX.label}" onclick="previousOffset();" />
						</td>
						<td>
							<input type="button"
								value="{$nextX.label}" onclick="nextOffset();" />
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
