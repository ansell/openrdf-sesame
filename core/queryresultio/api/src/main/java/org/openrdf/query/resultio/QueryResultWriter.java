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

import java.util.List;

import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;

/**
 * The base interface for writers of query results sets and boolean results.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.8.0
 */
public interface QueryResultWriter<T extends QueryResultFormat, E extends QueryResultHandlerException> extends QueryResultHandler {

	/**
	 * Gets the query result format that this writer uses.
	 * 
	 * @since 2.8.0
	 */
	T getQueryResultFormat();

	/**
	 * Indicates the start of the document.
	 * 
	 * @throws E
	 * @since 2.8.0
	 */
	void startDocument()
		throws E;

	/**
	 * Handles a stylesheet URL. If this is called, it must be called after
	 * {@link #startDocument} and before {@link #startHeader}. <br/>
	 * NOTE: If the format does not support stylesheets, it must silently ignore
	 * calls to this method.
	 * 
	 * @param stylesheetUrl
	 *        The URL of the stylesheet to be used to style the results.
	 * @throws E
	 * @since 2.8.0
	 */
	void handleStylesheet(String stylesheetUrl)
		throws E;

	/**
	 * Indicates the start of the header.
	 * 
	 * @see http://www.w3.org/TR/2012/PER-rdf-sparql-XMLres-20121108/#head
	 * @throws E
	 * @since 2.8.0
	 */
	void startHeader()
		throws E;

	/**
	 * Handles the insertion of links elements into the header. <br/>
	 * NOTE: If the format does not support links, it must silently ignore a call
	 * to this method.
	 * 
	 * @see http://www.w3.org/TR/sparql11-results-json/#select-link
	 * @param linkUrls
	 *        The URLs of the links to insert into the header.
	 * @throws E
	 * @since 2.8.0
	 */
	void handleLinks(List<String> linkUrls)
		throws E;

	/**
	 * Indicates the end of the header. This must be called after
	 * {@link #startHeader} and before any calls to {@link #handleSolution}.
	 * 
	 * @throws E
	 * @since 2.8.0
	 */
	void endHeader()
		throws E;

}
