/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.query.parser.ParsedQuery;

/**
 * @author Arjohn Kampman
 */
abstract class SailQuery extends AbstractQuery {

	protected ParsedQuery parsedQuery;

	protected SailRepositoryConnection con;

	protected SailQuery(ParsedQuery parsedQuery, SailRepositoryConnection con) {
		this.parsedQuery = parsedQuery;
		this.con = con;
	}

	ParsedQuery getParsedQuery() {
		return parsedQuery;
	}

	SailRepositoryConnection getConnection() {
		return con;
	}

	@Override
	public String toString()
	{
		return parsedQuery.toString();
	}
}
