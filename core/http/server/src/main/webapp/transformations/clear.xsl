<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#" xmlns:s="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">
	<xsl:include href="../locale/messages.xsl" />
	<xsl:include href="template.xsl" />
	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$clear.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId" select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>
	<xsl:template name="content">
		<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />
		<script type="text/javascript">
			<![CDATA[
			// Provide the XMLHttpRequest class for IE 5.x-6.x:
			if( typeof XMLHttpRequest == "undefined" ) XMLHttpRequest = function() {
				try { return new ActiveXObject("Msxml2.XMLHTTP.6.0") } catch(e) {}
				try { return new ActiveXObject("Msxml2.XMLHTTP.3.0") } catch(e) {}
				try { return new ActiveXObject("Msxml2.XMLHTTP") } catch(e) {}
				try { return new ActiveXObject("Microsoft.XMLHTTP") } catch(e) {}
				throw new Error( "This browser does not support XMLHttpRequest." )
			}
			function clear() {
				var context = document.getElementById('context');
				var url = document.getElementById('clear').action;
				if (context.value) {
					url += '?context=' + encodeURIComponent(context.value);
				}
				var request =  new XMLHttpRequest();
				request.open("DELETE", url, true);
				request.onreadystatechange = function() {
					if (request.readyState == 4) {
						if (request.status == 204) {
							location.href = "summary";
						} else {
							var p = document.createElement('p');
							p.setAttribute("class", "error");
							p.appendChild(document.createTextNode(request.responseText));
							document.getElementById('error').appendChild(p);
						}
					}
				};
				request.send(null);
			}
			]]>
		</script>
		<xsl:if test="//s:binding[@name='repositoryId']/s:literal/text() = 'SYSTEM'">
			<p class="WARN">
				<xsl:value-of select="$SYSTEM-warning.desc" />
			</p>
		</xsl:if>
		<p class="WARN">
			<xsl:value-of select="$clear-warning.desc" />
		</p>
		<div id="error">
			<xsl:if test="//s:binding[@name='error-message']">
				<p class="error">
					<xsl:value-of select="//s:binding[@name='error-message']" />
				</p>
			</xsl:if>
		</div>
		<form id="clear" name="clear" method="DELETE" action="{$repository}/statements">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$context.label" />
						</th>
						<td>
							<input id="context" name="context" type="text" value="{//s:binding[@name='context']/s:literal}" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit" onclick="clear();return false" value="{$clear-context.label}" />
						</td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</form>
	</xsl:template>
</xsl:stylesheet>