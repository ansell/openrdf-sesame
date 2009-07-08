/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import org.openrdf.query.BooleanQuery;
import org.openrdf.result.BooleanResult;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class SignedBooleanQuery extends SignedQuery implements BooleanQuery {

	public SignedBooleanQuery(BooleanQuery query, BNodeSigner signer) {
		super(query, signer);
	}

	@Override
	protected BooleanQuery getQuery() {
		return (BooleanQuery)super.getQuery();
	}

	public boolean ask()
		throws StoreException
	{
		return getQuery().ask();
	}

	public BooleanResult evaluate()
		throws StoreException
	{
		return getQuery().evaluate();
	}
}
