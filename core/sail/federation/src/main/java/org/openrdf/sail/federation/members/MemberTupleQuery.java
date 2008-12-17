/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.List;
import java.util.Map;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeFactoryImpl;
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
public class MemberTupleQuery extends MemberQuery implements TupleQuery {

	private TupleQuery query;

	public MemberTupleQuery(TupleQuery query, BNodeFactoryImpl bf, Map<BNode, BNode> in, Map<BNode, BNode> out) {
		super(query, bf, in, out);
		this.query = query;
	}

	public TupleResult evaluate()
		throws StoreException
	{
		final TupleResult result = query.evaluate();
		if (out.isEmpty())
			return result;
		return new TupleResultImpl(result.getBindingNames(), new Cursor<BindingSet>() {

			public void close()
				throws StoreException
			{
				result.close();
			}

			public BindingSet next()
				throws StoreException
			{
				return export(result.next());
			}
		});
	}

	public void evaluate(final TupleQueryResultHandler handler)
		throws StoreException, TupleQueryResultHandlerException
	{
		if (out.isEmpty()) {
			query.evaluate(handler);
		}
		else {
			query.evaluate(new TupleQueryResultHandler() {

				public void endQueryResult()
					throws TupleQueryResultHandlerException
				{
					handler.endQueryResult();
				}

				public void handleSolution(BindingSet bindingSet)
					throws TupleQueryResultHandlerException
				{
					handler.handleSolution(export(bindingSet));
				}

				public void startQueryResult(List<String> bindingNames)
					throws TupleQueryResultHandlerException
				{
					handler.startQueryResult(bindingNames);
				}
			});
		}
	}

}
