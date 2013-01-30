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

import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * The interface of objects that writer query results in a specific query result
 * format.
 */
public interface TupleQueryResultWriter extends TupleQueryResultHandler, QueryResultWriter {

	/**
	 * Gets the query result format that this writer uses.
	 */
	TupleQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Indicates the start of the document.
	 * 
	 * @throws TupleQueryResultHandlerException
	 * @since 2.8.0
	 */
	void startDocument()
		throws TupleQueryResultHandlerException;

	/**
	 * Handles a stylesheet URL. If this is called, it must be called after
	 * {@link #startDocument} and before {@link #startHeader}. <br/>
	 * NOTE: If the format does not support stylesheets, it must silently ignore
	 * calls to this method.
	 * 
	 * @param stylesheetUrl
	 *        The URL of the stylesheet to be used to style the results.
	 * @since 2.8.0
	 */
	void handleStylesheet(String stylesheetUrl)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the start of the header.
	 * 
	 * @see http://www.w3.org/TR/2012/PER-rdf-sparql-XMLres-20121108/#head
	 * @throws TupleQueryResultHandlerException
	 * @since 2.8.0
	 */
	void startHeader()
		throws TupleQueryResultHandlerException;

	/**
	 * Handles the insertion of links elements into the header. <br/>
	 * NOTE: If the format does not support links, it must silently ignore a call
	 * to this method.
	 * 
	 * @see http://www.w3.org/TR/sparql11-results-json/#select-link
	 * @param linkUrls
	 *        The URLs of the links to insert into the header.
	 * @throws TupleQueryResultHandlerException
	 * @since 2.8.0
	 */
	void handleLinks(List<String> linkUrls)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the end of the header. This must be called after
	 * {@link #startHeader} and before any calls to {@link #handleSolution}.
	 * 
	 * @throws TupleQueryResultHandlerException
	 * @since 2.8.0
	 */
	void endHeader()
		throws TupleQueryResultHandlerException;


}
