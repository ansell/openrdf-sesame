<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#" xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:variable name="repository" select="//s:binding[@name = 'repository']" />

	<xsl:variable name="namespaces" select="document(concat($repository, '/namespaces'))" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$namespaces.title" />
			</xsl:with-param>
			<xsl:with-param name="workbench" select="//s:binding[@name = 'workbench']" />
			<xsl:with-param name="server" select="//s:binding[@name = 'server']" />
			<xsl:with-param name="repository" select="//s:binding[@name = 'repository']" />
			<xsl:with-param name="repositoryId"
				select="//s:binding[@name = 'repositoryId']/s:literal/text()" />
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="s:literal">
		<xsl:value-of select="." />
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
			function updatePrefix() {
				var prefix = document.getElementById('prefix');
				var namespace = document.getElementById('namespace');
				var select = document.getElementById('prefix-select');
				prefix.value = select.options[select.selectedIndex].text;
				namespace.value = select.value;
			}
			function putNamespace() {
				var prefix = document.getElementById('prefix');
				var namespace = document.getElementById('namespace');
				var url = document.getElementById('namespaces').action + '/' + prefix.value;
				var request =  new XMLHttpRequest();
				request.open("PUT", url, true);
				request.setRequestHeader("Content-Type", "text/plain");
				request.onreadystatechange = function() {
					if (request.readyState == 4) {
						if (request.status == 204) {
							location.reload();
						} else {
							var p = document.createElement('p');
							p.setAttribute("class", "error");
							p.appendChild(document.createTextNode(request.responseText));
							document.getElementById('error').appendChild(p);
						}
					}
				};
				request.send(namespace.value);
			}
			function deleteNamespace() {
				var prefix = document.getElementById('prefix');
				var url = document.getElementById('namespaces').action + '/' + prefix.value;
				var request =  new XMLHttpRequest();
				request.open("DELETE", url, true);
				request.onreadystatechange = function() {
					if (request.readyState == 4 && request.status == 204) {
						location.reload();
					}
				};
				request.send(null);
			}
			]]>
		</script>
		<div id="error">
			<xsl:if test="//s:binding[@name='error-message']">
				<p class="error">
					<xsl:value-of select="//s:binding[@name='error-message']" />
				</p>
			</xsl:if>
		</div>
		<form id="namespaces" name="namespaces" action="{$repository}/namespaces">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$prefix.label" />
						</th>
						<td>
							<input type="text" id="prefix" name="prefix" size="8" />
							<select id="prefix-select" onchange="updatePrefix()">
								<option></option>
								<xsl:for-each select="$namespaces//s:result">
									<option value="{s:binding[@name='namespace']/s:literal}">
										<xsl:value-of select="s:binding[@name='prefix']/s:literal" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$namespace.label" />
						</th>
						<td>
							<input type="text" id="namespace" name="namespace" size="48" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td colspan="2">
							<input type="button" onclick="putNamespace();return false"
								value="{$update.label}" />
							<input type="button" onclick="deleteNamespace();return false"
								value="{$delete.label}" />
						</td>
					</tr>
				</tbody>
			</table>
		</form>
		<xsl:apply-templates mode="table" select="$namespaces" />
	</xsl:template>

</xsl:stylesheet>
