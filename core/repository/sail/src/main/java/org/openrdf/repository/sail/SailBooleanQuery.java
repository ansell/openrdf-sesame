/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.cursor.Cursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.parser.BooleanQueryModel;
import org.openrdf.result.BooleanResult;
import org.openrdf.result.impl.BooleanResultImpl;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SailBooleanQuery extends SailQuery implements BooleanQuery {

	protected SailBooleanQuery(BooleanQueryModel tupleQuery, SailRepositoryConnection sailConnection) {
		super(tupleQuery, sailConnection);
	}

	@Override
	public BooleanQueryModel getParsedQuery() {
		return (BooleanQueryModel)super.getParsedQuery();
	}

	public BooleanResult evaluate()
		throws StoreException
	{
		return new BooleanResultImpl(ask());
	}

	public boolean ask()
		throws StoreException
	{
		Cursor<? extends BindingSet> bindingsIter = evaluate(getParsedQuery());

		try {
			return bindingsIter.next() != null;
		}
		finally {
			bindingsIter.close();
		}
	}
}
