/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import static org.openrdf.query.QueryLanguage.SPARQL;

import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class TypesServlet extends TupleServlet {

	public TypesServlet() {
		super("types.xsl", "type");
	}

	private static final String DISTINCT_TYPE = "SELECT DISTINCT ?type WHERE { ?subj a ?type }";

	@Override
	protected void service(TupleResultBuilder builder, RepositoryConnection con)
			throws Exception {
		TupleQuery query = con.prepareTupleQuery(SPARQL, DISTINCT_TYPE);
		TupleQueryResult result = query.evaluate();
		try {
			while (result.hasNext()) {
				builder.result(result.next().getValue("type"));
			}
		} finally {
			result.close();
		}
	}

}