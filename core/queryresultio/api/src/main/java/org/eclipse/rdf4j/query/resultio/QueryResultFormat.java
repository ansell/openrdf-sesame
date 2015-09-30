/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.resultio;

import java.nio.charset.Charset;
import java.util.Collection;

import org.eclipse.rdf4j.common.lang.FileFormat;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;

/**
 * The base class of all file formats that represent the results of queries.
 * Currently this includes tuple and boolean queries.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class QueryResultFormat extends FileFormat {

	/**
	 * Local constant reused across boolean and tuple formats for SPARQL Results
	 * XML.
	 */
	protected static final IRI SPARQL_RESULTS_XML_URI = SimpleValueFactory.getInstance().createIRI(
			"http://www.w3.org/ns/formats/SPARQL_Results_XML");

	/**
	 * Local constant reused across boolean and tuple formats for SPARQL Results
	 * JSON.
	 */
	protected static final IRI SPARQL_RESULTS_JSON_URI = SimpleValueFactory.getInstance().createIRI(
			"http://www.w3.org/ns/formats/SPARQL_Results_JSON");

	/**
	 * Local constant for tuple formats for SPARQL Results CSV.
	 */
	protected static final IRI SPARQL_RESULTS_CSV_URI = SimpleValueFactory.getInstance().createIRI(
			"http://www.w3.org/ns/formats/SPARQL_Results_CSV");

	/**
	 * Local constant for tuple formats for SPARQL Results TSV.
	 */
	protected static final IRI SPARQL_RESULTS_TSV_URI = SimpleValueFactory.getInstance().createIRI(
			"http://www.w3.org/ns/formats/SPARQL_Results_TSV");

	/**
	 * A standard URI published by the W3C or another standards body to uniquely
	 * denote this format.
	 * 
	 * @see <a href="http://www.w3.org/ns/formats/">Unique URIs for File
	 *      Formats</a>
	 */
	private IRI standardURI;

	/**
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML file
	 *        format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExt
	 *        The (default) file extension for the format, e.g. <tt>srx</tt> for
	 *        SPARQL/XML files.
	 */
	public QueryResultFormat(String name, String mimeType, Charset charset, String fileExt) {
		super(name, mimeType, charset, fileExt);
	}

	/**
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExtensions
	 *        The format's file extensions, e.g. <tt>srx</tt> for SPARQL/XML
	 *        files. The first item in the list is interpreted as the default
	 *        file extension for the format.
	 */
	public QueryResultFormat(String name, String mimeType, Charset charset, Collection<String> fileExtensions)
	{
		super(name, mimeType, charset, fileExtensions);
	}

	/**
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeTypes
	 *        The MIME types of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML format.
	 *        The first item in the list is interpreted as the default MIME type
	 *        for the format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExtensions
	 *        The format's file extensions, e.g. <tt>srx</tt> for SPARQL/XML
	 *        files. The first item in the list is interpreted as the default
	 *        file extension for the format.
	 */
	public QueryResultFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions)
	{
		super(name, mimeTypes, charset, fileExtensions);
	}

	/**
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeTypes
	 *        The MIME types of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML format.
	 *        The first item in the list is interpreted as the default MIME type
	 *        for the format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExtensions
	 *        The format's file extensions, e.g. <tt>srx</tt> for SPARQL/XML
	 *        files. The first item in the list is interpreted as the default
	 *        file extension for the format.
	 * @since 2.8.0
	 */
	public QueryResultFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions, IRI standardURI)
	{
		super(name, mimeTypes, charset, fileExtensions);

		this.standardURI = standardURI;
	}

	/**
	 * @return True if a standard URI has been assigned to this format by a
	 *         standards organisation.
	 * @since 2.8.0
	 */
	public boolean hasStandardURI() {
		return standardURI != null;
	}

	/**
	 * @return The standard URI that has been assigned to this format by a
	 *         standards organisation or null if it does not currently have a
	 *         standard URI.
	 * @since 2.8.0
	 */
	public IRI getStandardURI() {
		return standardURI;
	}
}