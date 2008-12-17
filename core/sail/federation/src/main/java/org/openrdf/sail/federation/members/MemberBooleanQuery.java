/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.impl.BNodeFactoryImpl;
import org.openrdf.query.BooleanQuery;
import org.openrdf.result.BooleanResult;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class MemberBooleanQuery extends MemberQuery implements BooleanQuery {

	private BooleanQuery query;

	public MemberBooleanQuery(BooleanQuery query, BNodeFactoryImpl bf, Map<BNode, BNode> in, Map<BNode, BNode> out) {
		super(query, bf, in, out);
		this.query = query;
	}

	public boolean ask()
		throws StoreException
	{
		return query.ask();
	}

	public BooleanResult evaluate()
		throws StoreException
	{
		return query.evaluate();
	}

}
