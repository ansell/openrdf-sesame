/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;

/**
 * <p>
 * Interface for Sesame-based query renderers
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public interface QueryRenderer {

	/**
	 * Return the language this QueryRenderer will render queries in.
	 * 
	 * @return the query language
	 */
	public QueryLanguage getLanguage();

	/**
	 * Render the query object to a string in the language supported by this
	 * renderer
	 * 
	 * @param theQuery
	 *        the query to render
	 * @return the rendered query
	 * @throws Exception
	 *         if there is an error while rendering
	 */
	public String render(ParsedQuery theQuery)
		throws Exception;
}
