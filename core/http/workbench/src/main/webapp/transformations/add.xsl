<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$add.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:template match="sparql:sparql">
		<xsl:if
			test="$info//sparql:binding[@name='id']/sparql:literal/text() = 'SYSTEM'">
			<p class="WARN">
				<xsl:value-of select="$SYSTEM-warning.desc" />
			</p>
		</xsl:if>
		<script type="text/javascript">
			<![CDATA[
			function enabledInput(selected) {
				document.getElementById('source-' + selected).checked = true;
				document.getElementById('file').disabled = selected != 'file';
				document.getElementById('url').disabled = selected != 'url';
				document.getElementById('text').disabled = selected != 'text';
				var baseURI = document.getElementById('baseURI');
				if (!baseURI.value) if (selected == 'file') {
					baseURI.value = encodeURI('file://' + document.getElementById('file').value.replace(/\\/g, '/'));
				}
				if (!baseURI.value) if (selected == 'url') {
					baseURI.value = document.getElementById('url').value;
				}
				var context = document.getElementById('context');
				if (!context.value) if (selected == 'file') {
					context.value = decodeURIComponent('%3C') + encodeURI('file://' + document.getElementById('file').value.replace(/\\/g, '/')) + decodeURIComponent('%3E');
				}
				if (!context.value) if (selected == 'url') {
					context.value = decodeURIComponent('%3C') + document.getElementById('url').value + decodeURIComponent('%3E');
				}
			}
			]]>
		</script>
		<xsl:if test="//sparql:binding[@name='error-message']">
			<p class="error">
				<xsl:value-of
					select="//sparql:binding[@name='error-message']" />
			</p>
		</xsl:if>
		<form method="post" action="add"
			enctype="multipart/form-data">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$base-uri.label" />
						</th>
						<td>
							<input id="baseURI" name="baseURI"
								type="text"
								value="{//sparql:binding[@name='baseURI']/sparql:literal}" />
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
								value="{//sparql:binding[@name='context']/sparql:literal}" />
						</td>
						<td></td>

					</tr>
					<tr>
						<th>
							<xsl:value-of select="$data-format.label" />
						</th>
						<td>
							<select id="Content-Type"
								name="Content-Type"
								onchange="saveCookie('Content-Type', this.value, 30)">
								<xsl:for-each
									select="document(//sparql:link/@href)//sparql:binding[@name='upload-format']">
									<option
										value="{substring-before(sparql:literal, ' ')}">
										<xsl:if
											test="$info//sparql:binding[@name='default-Content-Type']/sparql:literal = substring-before(sparql:literal, ' ')">
											<xsl:attribute
												name="selected">true</xsl:attribute>
										</xsl:if>
										<xsl:value-of
											select="substring-after(sparql:literal, ' ')" />
									</option>
								</xsl:for-each>
							</select>
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>
						<td>
							<input type="radio" id="source-url"
								name="source" value="url" onchange="enabledInput('url')" />
							<xsl:value-of select="$upload-url.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$upload-url.label" />
						</th>
						<td>
							<input id="url" name="url" type="text"
								value="" onchange="enabledInput('url');" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="radio" id="source-file"
								name="source" value="file" onchange="enabledInput('file')" />
							<xsl:value-of select="$upload-file.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$upload-file.label" />
						</th>
						<td>
							<input type="file" id="file" name="content"
								onchange="enabledInput('file')" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<input type="radio" id="source-text"
								name="source" value="contents" onchange="enabledInput('text')" />
							<xsl:value-of select="$upload-text.desc" />
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$upload-text.label" />
						</th>
						<td>
							<textarea id="text" name="content" rows="6"
								cols="70" onchange="enabledInput('text')">
							</textarea>
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>

						<td>
							<input type="submit" value="Upload" />
						</td>
						<td></td>
					</tr>
				</tbody>
			</table>

		</form>
	</xsl:template>

</xsl:stylesheet>
