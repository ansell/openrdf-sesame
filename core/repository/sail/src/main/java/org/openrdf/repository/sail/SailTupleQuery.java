/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
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
import org.openrdf.query.algebra.Slice;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SailTupleQuery extends SailQuery implements TupleQuery {

	protected int offset = 0;

	protected int limit = -1;

	protected SailTupleQuery(TupleQueryModel tupleQuery, SailRepositoryConnection sailConnection) {
		super(tupleQuery, sailConnection);
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOffset() {
		return offset;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	@Override
	public TupleQueryModel getParsedQuery() {
		return (TupleQueryModel)super.getParsedQuery();
	}

	public TupleResult evaluate()
		throws StoreException
	{
		TupleQueryModel query = getParsedQuery();

		if (getOffset() > 0 || getLimit() >= 0) {
			query.setArg(new Slice(query.getArg(), getOffset(), getLimit()));
		}

		Cursor<? extends BindingSet> bindingsIter = evaluate(query);

		return new TupleResultImpl(new ArrayList<String>(query.getBindingNames()), bindingsIter);
	}

	public <H extends TupleQueryResultHandler> H evaluate(H handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		TupleResult queryResult = evaluate();
		QueryResultUtil.report(queryResult, handler);
		return handler;
	}
}
