/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.BooleanResult;
import org.openrdf.query.Cursor;
import org.openrdf.query.impl.BooleanResultImpl;
import org.openrdf.query.parser.BooleanQueryModel;
import org.openrdf.sail.SailConnection;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
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
		SailConnection sailCon = getConnection().getSailConnection();

		Cursor<? extends BindingSet> bindingsIter;
		bindingsIter = sailCon.evaluate(getParsedQuery(), getBindings(), getIncludeInferred());

		bindingsIter = enforceMaxQueryTime(bindingsIter);

		try {
			return bindingsIter.next() != null;
		}
		finally {
			bindingsIter.close();
		}
	}
}
