/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.model.BNode;
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

	Set<BNode> contains;

	public MemberTupleQuery(TupleQuery query, Map<BNode, BNode> in, Map<BNode, BNode> out, Set<BNode> contains)
	{
		super(query, in, out);
		this.query = query;
		this.contains = contains;
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
				return export(result.next(), contains);
			}
		});
	}

	public void evaluate(final TupleQueryResultHandler handler)
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
				handler.handleSolution(export(bindingSet, contains));
			}

			public void startQueryResult(List<String> bindingNames)
				throws TupleQueryResultHandlerException
			{
				handler.startQueryResult(bindingNames);
			}
		});
	}

}
