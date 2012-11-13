/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import org.openrdf.OpenRDFException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;

/**
 * Utility class for generating query objects.
 */
public class QueryFactory {

	public static Query prepareQuery(final RepositoryConnection con, final QueryLanguage queryLn, final String query)
		throws OpenRDFException
	{
		Query rval = null;
		try {
			rval = con.prepareQuery(queryLn, query);
		}
		catch (UnsupportedOperationException exc) {
			// TODO must be an HTTP repository
			try {
				con.prepareTupleQuery(queryLn, query).evaluate().close();
				rval = con.prepareTupleQuery(queryLn, query);
			}
			catch (Exception e1) {
				// guess its not a tuple query
				try {
					con.prepareGraphQuery(queryLn, query).evaluate().close();
					rval = con.prepareGraphQuery(queryLn, query);
				}
				catch (Exception e2) {
					// guess its not a graph query
					try {
						con.prepareBooleanQuery(queryLn, query).evaluate();
						rval = con.prepareBooleanQuery(queryLn, query);
					}
					catch (Exception e3) {
						// guess its not a boolean query
						// let's assume it is an malformed tuple query
						rval = con.prepareTupleQuery(queryLn, query);
					}
				}
			}
		}
		return rval;
	}
}
