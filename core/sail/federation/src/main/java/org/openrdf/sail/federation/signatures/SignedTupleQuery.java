/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import java.util.List;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelegatingCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.result.TupleResult;
import org.openrdf.result.impl.TupleResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class SignedTupleQuery extends SignedQuery implements TupleQuery {

	public SignedTupleQuery(TupleQuery query, BNodeSigner signer) {
		super(query, signer);
	}

	@Override
	protected TupleQuery getQuery() {
		return (TupleQuery)super.getQuery();
	}

	public int getLimit() {
		return getQuery().getLimit();
	}

	public void setLimit(int limit) {
		getQuery().setLimit(limit);
	}

	public int getOffset() {
		return getQuery().getOffset();
	}

	public void setOffset(int offset) {
		getQuery().setOffset(offset);
	}

	public TupleResult evaluate()
		throws StoreException
	{
		TupleResult result = getQuery().evaluate();

		Cursor<BindingSet> signedBindings = new DelegatingCursor<BindingSet>(result) {

			@Override
			public BindingSet next()
				throws StoreException
			{
				return getSigner().sign(super.next());
			}
		};

		return new TupleResultImpl(result.getBindingNames(), signedBindings);
	}

	public <H extends TupleQueryResultHandler> H evaluate(final H handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		getQuery().evaluate(new TupleQueryResultHandler() {

			public void startQueryResult(List<String> bindingNames)
				throws TupleQueryResultHandlerException
			{
				handler.startQueryResult(bindingNames);
			}

			public void endQueryResult()
				throws TupleQueryResultHandlerException
			{
				handler.endQueryResult();
			}

			public void handleSolution(BindingSet bindingSet)
				throws TupleQueryResultHandlerException
			{
				handler.handleSolution(getSigner().sign(bindingSet));
			}
		});

		return handler;
	}

}
