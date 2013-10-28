/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.query.resultio;

import java.nio.charset.Charset;
import java.util.Collection;

import info.aduna.lang.FileFormat;

import org.openrdf.model.URI;

/**
 * The base class of all file formats that represent the results of queries.
 * Currently this includes tuple and boolean queries.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class QueryResultFormat extends FileFormat {

	/**
	 * A standard URI published by the W3C or another standards body to uniquely
	 * denote this format.
	 * 
	 * @see <a href="http://www.w3.org/ns/formats/">Unique URIs for File
	 *      Formats</a>
	 */
	private URI standardURI;

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
			Collection<String> fileExtensions, URI standardURI)
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
	public URI getStandardURI() {
		return standardURI;
	}
}