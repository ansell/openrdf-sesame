/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryrender.serql;

import org.openrdf.query.QueryLanguage;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.queryrender.QueryRenderer;

/**
 * <p>
 * Implementation of the {@link QueryRenderer} interface which renders
 * {@link org.openrdf.query.parser.ParsedQuery} objects as strings in SeRQL
 * syntax
 * </p>
 * 
 * @author Michael Grove
 * @since 2.7.0
 */
public class SeRQLQueryRenderer implements QueryRenderer {

	public final static boolean SERQL_ONE_X_COMPATIBILITY_MODE = false;

	/**
	 * The renderer object
	 */
	private SerqlTupleExprRenderer mRenderer = new SerqlTupleExprRenderer();

	/**
	 * @inheritDoc
	 */
	public QueryLanguage getLanguage() {
		return QueryLanguage.SERQL;
	}

	/**
	 * @inheritDoc
	 */
	public String render(final ParsedQuery theQuery)
		throws Exception
	{
		mRenderer.reset();

		return mRenderer.render(theQuery.getTupleExpr());
	}
}
