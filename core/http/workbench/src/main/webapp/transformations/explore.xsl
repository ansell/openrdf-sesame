<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE rdf:RDF [
   <!ENTITY xsd  "http://www.w3.org/2001/XMLSchema#" >
   <!ENTITY rdf  "http://www.w3.org/1999/02/22-rdf-syntax-ns#" >
   <!ENTITY rdfs  "http://www.w3.org/2000/01/rdf-schema#" >
 ]>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	xmlns:sparql="http://www.w3.org/2005/sparql-results#"
	xmlns="http://www.w3.org/1999/xhtml">

	<xsl:include href="../locale/messages.xsl" />

	<xsl:variable name="title">
		<xsl:value-of select="$explore.title" />
	</xsl:variable>

	<xsl:include href="template.xsl" />

	<xsl:include href="table.xsl" />

	<xsl:template name="sort-list">
		<xsl:param name="title" />
		<xsl:param name="list" />
		<div>
			<h3>
				<xsl:value-of select="$title" />
			</h3>
			<ul>
				<xsl:for-each select="$list">
					<xsl:sort select="." />
					<li>
						<xsl:apply-templates select="." />
					</li>
				</xsl:for-each>
			</ul>
		</div>
	</xsl:template>

	<xsl:template match="sparql:sparql">
		<script type="text/javascript">
			<![CDATA[
			function textContent(element) {
				var text = element.innerText || element.textContent;
				return text.replace(/^\s*/, "").replace(/\s*$/, "");
			}
			function removeDuplicates(self) {
				var lists = document.getElementsByTagName('ul');
				for (var i=lists.length-1;i + 1;i--) {
					var items = lists[i].getElementsByTagName('li');
					for (var j=items.length - 1;j;j--) {
						var text = textContent(items[j]);
						if (items[j].innerHTML == items[j-1].innerHTML || text == self) {
							items[j].parentNode.removeChild(items[j]);
						}
					}
					text = textContent(items[0]);
					if (text == self) {
						items[0].parentNode.removeChild(items[0]);
					}
					if (items.length == 0) {
						lists[i].parentNode.parentNode.removeChild(lists[i].parentNode);
					}
				}
			}
			function populateParameters() {
				var href = document.location.href;
				var elements = href.substring(href.indexOf('?') + 1).split(decodeURIComponent('%26'));
				for (var i=0;elements.length-i;i++) {
					var pair = elements[i].split('=');
					var value = decodeURIComponent(pair[1]).replace(/\+/g, ' ');
					if (pair[0] == 'resource') {
						document.getElementById('resource').value = value;
					}
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
			window.onload = function() {
				populateParameters();
				var title = document.getElementById('content').getElementsByTagName('h1')[0];
				var value = document.getElementById('resource').value;
				if (value) {
					title.appendChild(document.createTextNode(' (' + value + ')'));
				}
				removeDuplicates(value);
			}
			]]>
		</script>
		<xsl:if test="$info//sparql:binding[@name='default-limit']/sparql:literal = count(//sparql:result)">
		<p id="result-limited">
			<xsl:value-of select="$result-limited.desc" />
		</p>
		</xsl:if>
		<xsl:if
			test="1 = count(//sparql:result/sparql:binding[@name='predicate']/sparql:uri[text() = '&rdfs;label'])">
			<xsl:for-each
				select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;label']">
				<h2>
					<xsl:value-of
						select="sparql:binding[@name='object']/sparql:literal" />
				</h2>
			</xsl:for-each>
		</xsl:if>
		<xsl:if
			test="1 = count(//sparql:result/sparql:binding[@name='predicate']/sparql:uri[text() = '&rdfs;comment'])">
			<xsl:for-each
				select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;comment']">
				<p>
					<xsl:value-of
						select="sparql:binding[@name='object']/sparql:literal" />
				</p>
			</xsl:for-each>
		</xsl:if>
		<table class="simple">
			<tr>
				<td>
					<xsl:if
						test="//sparql:result/sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;subClassOf'">
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title"
								select="$super-classes.title" />
							<xsl:with-param name="list"
								select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;subClassOf']/sparql:binding[@name='object']" />
						</xsl:call-template>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title"
								select="$sub-classes.title" />
							<xsl:with-param name="list"
								select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;subClassOf']/sparql:binding[@name='subject']" />
						</xsl:call-template>
					</xsl:if>
				</td>
				<td>
					<xsl:if
						test="//sparql:result/sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;domain'">
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title"
								select="$properties.title" />
							<xsl:with-param name="list"
								select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;domain']/sparql:binding[@name='subject']" />
						</xsl:call-template>
						<xsl:if
							test="//sparql:result/sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;subPropertyOf'">
							<xsl:call-template name="sort-list">
								<xsl:with-param name="title"
									select="$super-properties.title" />
								<xsl:with-param name="list"
									select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;subPropertyOf']/sparql:binding[@name='object']" />
							</xsl:call-template>
							<xsl:call-template name="sort-list">
								<xsl:with-param name="title"
									select="$sub-properties.title" />
								<xsl:with-param name="list"
									select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;subPropertyOf']/sparql:binding[@name='subject']" />
							</xsl:call-template>
						</xsl:if>
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title"
								select="$property-domain.title" />
							<xsl:with-param name="list"
								select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;domain']/sparql:binding[@name='object']" />
						</xsl:call-template>
					</xsl:if>
					<xsl:if
						test="//sparql:result/sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;range'">
						<xsl:call-template name="sort-list">
							<xsl:with-param name="title"
								select="$property-range.title" />
							<xsl:with-param name="list"
								select="//sparql:result[sparql:binding[@name='predicate']/sparql:uri/text() = '&rdfs;range']/sparql:binding[@name='object']" />
						</xsl:call-template>
					</xsl:if>
				</td>
			</tr>
		</table>
		<xsl:if
			test="sparql:head/sparql:variable/@name != 'error-message' and sparql:results">
			<table class="data">
				<xsl:apply-templates select="*" />
			</table>
		</xsl:if>
		<form action="explore">
			<table class="dataentry">
				<tbody>
					<tr>
						<th>
							<xsl:value-of select="$resource.label" />
						</th>
						<td>
							<input id="resource" name="resource"
								size="48" type="text" />
						</td>
						<td></td>
					</tr>
					<tr>
						<td></td>
						<td>
							<span class="error">
								<xsl:value-of
									select="//sparql:binding[@name='error-message']" />
							</span>
						</td>
						<td></td>
					</tr>
					<tr>
						<th>
							<xsl:value-of select="$result-limit.label" />
						</th>
						<td>
							<xsl:call-template name="limit-select"/>
						</td>
						<td></td>
					</tr>

					<tr>
						<td></td>
						<td>
							<input type="submit" value="{$show.label}" />
						</td>
						<td></td>
					</tr>
				</tbody>
			</table>
		</form>
	</xsl:template>

</xsl:stylesheet>
