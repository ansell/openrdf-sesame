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

import java.util.Collection;

import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.rio.RioSetting;
import org.openrdf.rio.WriterConfig;

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
	 * @see http://www.w3.org/TR/2012/PER-rdf-sparql-XMLres-20121108/#head
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
