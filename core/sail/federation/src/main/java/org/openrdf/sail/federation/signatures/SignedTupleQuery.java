/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import java.util.List;

import org.openrdf.cursor.Cursor;
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

	private TupleQuery query;

	public SignedTupleQuery(TupleQuery query, BNodeSigner signer) {
		super(query, signer);
		this.query = query;
	}

	public TupleResult evaluate()
		throws StoreException
	{
		final TupleResult result = query.evaluate();
		return new TupleResultImpl(result.getBindingNames(), new Cursor<BindingSet>() {

			public void close()
				throws StoreException
			{
				result.close();
			}

			public BindingSet next()
				throws StoreException
			{
				return signer.sign(result.next());
			}
		});
	}

	public <H extends TupleQueryResultHandler> H evaluate(final H handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		query.evaluate(new TupleQueryResultHandler() {

			public void endQueryResult()
				throws TupleQueryResultHandlerException
			{
				handler.endQueryResult();
			}

			public void handleSolution(BindingSet bindingSet)
				throws TupleQueryResultHandlerException
			{
				handler.handleSolution(signer.sign(bindingSet));
			}

			public void startQueryResult(List<String> bindingNames)
				throws TupleQueryResultHandlerException
			{
				handler.startQueryResult(bindingNames);
			}
		});
		return handler;
	}

}
