<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:s="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:include href="template.xsl" />

	<xsl:template match="/">
		<xsl:call-template name="html">
			<xsl:with-param name="title">
				<xsl:value-of select="$remove.title" />
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
			function remove() {
				var subj = document.getElementById('subj');
				var pred = document.getElementById('pred');
				var obj = document.getElementById('obj');
				var context = document.getElementById('context');
				var url = document.getElementById('remove').action;
				var qs = [];
				if (subj.value) {
					qs[qs.length] = 'subj=' + encodeURIComponent(subj.value);
				}
				if (pred.value) {
					qs[qs.length] = 'pred=' + encodeURIComponent(pred.value);
				}
				if (obj.value) {
					qs[qs.length] = 'obj=' + encodeURIComponent(obj.value);
				}
				if (context.value) {
					qs[qs.length] = 'context=' + encodeURIComponent(context.value);
				}
				if (qs.length) {
					url += '?' + qs.join('&');
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
		<xsl:if
			test="//s:binding[@name='repositoryId']/s:literal/text() = 'SYSTEM'">
			<p class="WARN">
				<xsl:value-of select="$SYSTEM-warning.desc" />
			</p>
		</xsl:if>
		<p>
			<xsl:value-of select="$value-encoding.desc" />
		</p>
		<ul>
			<li>
				URI:
				<tt>&lt;http://foo.com/bar&gt;</tt>
			</li>
			<li>
				BNode:
				<tt>_:nodeID</tt>
			</li>
			<li>
				Literal:
				<tt>"Hello"</tt>
				,
				<tt>"Hello"@en</tt>
				and
				<tt>"Hello"^^&lt;http://bar.com/foo&gt;</tt>
			</li>

		</ul>
		<div id="error">
		<xsl:if test="//s:binding[@name='error-message']">
			<p class="error">
				<xsl:value-of
					select="//s:binding[@name='error-message']" />
			</p>
		</xsl:if>
		</div>
		<form id="remove" name="remove" method="DELETE" action="{$repository}/statements">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$subject.label" />
						</th>
						<td>
							<input id="subj" name="subj" type="text"
								value="{//s:binding[@name='subj']/s:literal}" />
						</td>
						<td></td>

					</tr>
					<tr>
						<th>
							<xsl:value-of select="$predicate.label" />
						</th>
						<td>
							<input id="pred" name="pred" type="text"
								value="{//s:binding[@name='pred']/s:literal}" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$object.label" />
						</th>

						<td>
							<textarea id="obj" name="obj" type="text"
								cols="70"
								value="{//s:binding[@name='obj']/s:literal}">
							</textarea>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$context.label" />
						</th>

						<td>
							<input id="context" name="context"
								type="text"
								value="{//s:binding[@name='context']/s:literal}" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="submit" onclick="remove();return false"
								value="{$remove.label}" />
						</td>
						<td></td>
					</tr>
				</tbody>

			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
