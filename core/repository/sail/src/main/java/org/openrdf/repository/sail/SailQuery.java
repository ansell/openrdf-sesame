/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.query.impl.TimeLimitCursor;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public abstract class SailQuery extends AbstractQuery {

	private final ParsedQuery parsedQuery;

	private final SailRepositoryConnection con;

	protected SailQuery(ParsedQuery parsedQuery, SailRepositoryConnection con) {
		this.parsedQuery = parsedQuery;
		this.con = con;
	}

	public ParsedQuery getParsedQuery() {
		return parsedQuery;
	}

	protected SailRepositoryConnection getConnection() {
		return con;
	}

	protected Cursor<? extends BindingSet> enforceMaxQueryTime(
			Cursor<? extends BindingSet> bindingsIter)
	{
		if (maxQueryTime > 0) {
			bindingsIter = new QueryInterruptCursor(bindingsIter, 1000L * maxQueryTime);
		}

		return bindingsIter;
	}

	/**
	 * Gets the "active" dataset for this query. The active dataset is either the
	 * dataset that has been specified using {@link #setDataset(Dataset)} or the
	 * dataset that has been specified in the query, where the former takes
	 * precedence over the latter.
	 * 
	 * @return The active dataset, or <tt>null</tt> if there is no dataset.
	 */
	public Dataset getActiveDataset() {
		if (dataset != null) {
			return dataset;
		}

		// No external dataset specified, use query's own dataset (if any)
		return parsedQuery.getDataset();
	}

	@Override
	public String toString() {
		return parsedQuery.toString();
	}

	protected class QueryInterruptCursor extends TimeLimitCursor<BindingSet> {

		public QueryInterruptCursor(Cursor<? extends BindingSet> iter,
				long timeLimit)
		{
			super(iter, timeLimit);
		}

		@Override
		protected void throwInterruptedException()
			throws StoreException
		{
			throw new QueryInterruptedException("Query evaluation took too long");
		}
	}
}
