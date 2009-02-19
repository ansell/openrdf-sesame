/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
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

	private BooleanQuery query;

	public SignedBooleanQuery(BooleanQuery query, BNodeSigner signer) {
		super(query, signer);
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
