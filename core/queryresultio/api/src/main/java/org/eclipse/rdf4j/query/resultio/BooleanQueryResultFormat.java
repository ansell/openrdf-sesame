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
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.rdf4j.model.IRI;

/**
 * Represents the concept of a boolean query result serialization format.
 * Boolean query result formats are identified by a {@link #getName() name} and
 * can have one or more associated MIME types, zero or more associated file
 * extensions and can specify a (default) character encoding.
 * 
 * @author Arjohn Kampman
 */
public class BooleanQueryResultFormat extends QueryResultFormat {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * SPARQL Query Results XML Format.
	 */
	public static final BooleanQueryResultFormat SPARQL = new BooleanQueryResultFormat("SPARQL/XML",
			Arrays.asList("application/sparql-results+xml", "application/xml"), Charset.forName("UTF-8"),
			Arrays.asList("srx", "xml"), SPARQL_RESULTS_XML_URI);

	/**
	 * SPARQL Query Results JSON Format.
	 */
	public static final BooleanQueryResultFormat JSON = new BooleanQueryResultFormat("SPARQL/JSON",
			Arrays.asList("application/sparql-results+json", "application/json"), Charset.forName("UTF-8"),
			Arrays.asList("srj", "json"), SPARQL_RESULTS_JSON_URI);

	/**
	 * Plain text encoding using values "true" and "false" (case-insensitive).
	 */
	public static final BooleanQueryResultFormat TEXT = new BooleanQueryResultFormat("TEXT", "text/boolean",
			Charset.forName("US-ASCII"), "txt");

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new BooleanQueryResultFormat object.
	 * 
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML format.
	 * @param fileExt
	 *        The (default) file extension for the format, e.g. <tt>srx</tt> for
	 *        SPARQL/XML.
	 */
	public BooleanQueryResultFormat(String name, String mimeType, String fileExt) {
		this(name, mimeType, null, fileExt);
	}

	/**
	 * Creates a new BooleanQueryResultFormat object.
	 * 
	 * @param name
	 *        The name of the format, e.g. "SPARQL/XML".
	 * @param mimeType
	 *        The MIME type of the format, e.g.
	 *        <tt>application/sparql-results+xml</tt> for the SPARQL/XML format.
	 * @param charset
	 *        The default character encoding of the format. Specify <tt>null</tt>
	 *        if not applicable.
	 * @param fileExt
	 *        The (default) file extension for the format, e.g. <tt>srx</tt> for
	 *        SPARQL/XML.
	 */
	public BooleanQueryResultFormat(String name, String mimeType, Charset charset, String fileExt) {
		super(name, mimeType, charset, fileExt);
	}

	/**
	 * Creates a new BooleanQueryResultFormat object.
	 * 
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
	public BooleanQueryResultFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions)
	{
		super(name, mimeTypes, charset, fileExtensions);
	}

	/**
	 * Creates a new BooleanQueryResultFormat object.
	 * 
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
	 * @param standardURI
	 *        The standard URI that has been assigned to this format by a
	 *        standards organisation or null if it does not currently have a
	 *        standard URI.
	 * @since 2.8.0
	 */
	public BooleanQueryResultFormat(String name, Collection<String> mimeTypes, Charset charset,
			Collection<String> fileExtensions, IRI standardURI)
	{
		super(name, mimeTypes, charset, fileExtensions, standardURI);
	}
}
