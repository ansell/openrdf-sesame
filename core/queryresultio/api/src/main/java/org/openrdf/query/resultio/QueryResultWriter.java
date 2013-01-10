/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2013.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.util.List;

import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * The base class for writers of query results sets and boolean results.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public interface QueryResultWriter {

	/**
	 * Indicates the start of the document.
	 * 
	 * @throws TupleQueryResultHandlerException
	 */
	public void startDocument()
		throws TupleQueryResultHandlerException;

	/**
	 * Handles a stylesheet URL. If this is called, it must be called after
	 * {@link #startDocument} and before {@link #startHeader}. <br/>
	 * NOTE: If the format does not support stylesheets, it must silently ignore
	 * calls to this method.
	 * 
	 * @param stylesheetUrl
	 *        The URL of the stylesheet to be used to style the results.
	 */
	public void handleStylesheet(String stylesheetUrl)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the start of the header.
	 * 
	 * @see http://www.w3.org/TR/2012/PER-rdf-sparql-XMLres-20121108/#head
	 * @throws TupleQueryResultHandlerException
	 */
	public void startHeader()
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
	 */
	public void handleLinks(List<String> linkUrls)
		throws TupleQueryResultHandlerException;

	/**
	 * Indicates the end of the header. This must be called after
	 * {@link #startHeader} and before any calls to {@link #handleSolution}.
	 * 
	 * @throws TupleQueryResultHandlerException
	 */
	public void endHeader()
		throws TupleQueryResultHandlerException;

}
