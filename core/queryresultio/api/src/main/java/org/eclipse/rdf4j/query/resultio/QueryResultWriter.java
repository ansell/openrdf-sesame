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

import java.util.Collection;

import org.eclipse.rdf4j.query.QueryResultHandler;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.rio.RioSetting;
import org.eclipse.rdf4j.rio.WriterConfig;

/**
 * The base interface for writers of query results sets and boolean results.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public interface QueryResultWriter extends QueryResultHandler {

	/**
	 * Gets the query result format that this writer uses.
	 * 
	 * @since 2.7.0
	 */
	QueryResultFormat getQueryResultFormat();

	/**
	 * Handles a namespace prefix declaration. If this is called, it should be
	 * called before {@link #startDocument()} to ensure that it has a document
	 * wide effect.
	 * <p>
	 * NOTE: If the format does not support namespaces, it must silently ignore
	 * calls to this method.
	 * 
	 * @param prefix
	 *        The prefix to use for the namespace
	 * @param uri
	 *        The full URI that is to be represented by the prefix.
	 * @throws QueryResultHandlerException
	 * @since 2.7.0
	 */
	void handleNamespace(String prefix, String uri)
		throws QueryResultHandlerException;

	/**
	 * Indicates the start of the document.
	 * 
	 * @throws QueryResultHandlerException
	 *         If there was an error starting the writing of the results.
	 * @since 2.7.0
	 */
	void startDocument()
		throws QueryResultHandlerException;

	/**
	 * Handles a stylesheet URL. If this is called, it must be called after
	 * {@link #startDocument} and before {@link #startHeader}.
	 * <p>
	 * NOTE: If the format does not support stylesheets, it must silently ignore
	 * calls to this method.
	 * 
	 * @param stylesheetUrl
	 *        The URL of the stylesheet to be used to style the results.
	 * @throws QueryResultHandlerException
	 *         If there was an error handling the stylesheet. This error is not
	 *         thrown in cases where stylesheets are not supported.
	 * @since 2.7.0
	 */
	void handleStylesheet(String stylesheetUrl)
		throws QueryResultHandlerException;

	/**
	 * Indicates the start of the header.
	 * 
	 * @see <a
	 *      href="http://www.w3.org/TR/2012/PER-rdf-sparql-XMLres-20121108/#head">SPARQL
	 *      Query Results XML Format documentation for head element.</a>
	 * @throws QueryResultHandlerException
	 *         If there was an error writing the start of the header.
	 * @since 2.7.0
	 */
	void startHeader()
		throws QueryResultHandlerException;

	/**
	 * Indicates the end of the header. This must be called after
	 * {@link #startHeader} and before any calls to {@link #handleSolution}.
	 * 
	 * @throws QueryResultHandlerException
	 *         If there was an error writing the end of the header.
	 * @since 2.7.0
	 */
	void endHeader()
		throws QueryResultHandlerException;

	/**
	 * Sets all supplied writer configuration options.
	 * 
	 * @param config
	 *        a writer configuration object.
	 * @since 2.7.0
	 */
	public void setWriterConfig(WriterConfig config);

	/**
	 * Retrieves the current writer configuration as a single object.
	 * 
	 * @return a writer configuration object representing the current
	 *         configuration of the writer.
	 * @since 2.7.0
	 */
	public WriterConfig getWriterConfig();

	/**
	 * @return A collection of {@link RioSetting}s that are supported by this
	 *         {@link QueryResultWriter}.
	 * @since 2.7.0
	 */
	public Collection<RioSetting<?>> getSupportedSettings();

}
