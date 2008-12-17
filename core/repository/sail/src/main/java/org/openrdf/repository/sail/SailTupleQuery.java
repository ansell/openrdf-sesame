/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import java.util.ArrayList;

import org.openrdf.cursor.Cursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class SailTupleQuery extends SailQuery implements TupleQuery {

	protected SailTupleQuery(TupleQueryModel tupleQuery, SailRepositoryConnection sailConnection) {
		super(tupleQuery, sailConnection);
	}

	@Override
	public TupleQueryModel getParsedQuery() {
		return (TupleQueryModel)super.getParsedQuery();
	}

	public TupleResult evaluate()
		throws StoreException
	{
		TupleQueryModel query = getParsedQuery();

		Cursor<? extends BindingSet> bindingsIter;
		SailConnection sailCon = getConnection().getSailConnection();
		bindingsIter = sailCon.evaluate(query, getBindings(), getIncludeInferred());

		bindingsIter = enforceMaxQueryTime(bindingsIter);

		return new TupleResultImpl(new ArrayList<String>(query.getBindingNames()), bindingsIter);
	}

	public void evaluate(TupleQueryResultHandler handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		TupleResult queryResult = evaluate();
		QueryResultUtil.report(queryResult, handler);
	}
}
