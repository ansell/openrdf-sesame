/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.signatures;

import org.openrdf.model.Statement;
import org.openrdf.query.GraphQuery;
import org.openrdf.result.GraphResult;
import org.openrdf.result.impl.GraphResultImpl;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerWrapper;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class SignedGraphQuery extends SignedQuery implements GraphQuery {

	public SignedGraphQuery(GraphQuery query, BNodeSigner signer) {
		super(query, signer);
	}

	@Override
	protected GraphQuery getQuery() {
		return (GraphQuery)super.getQuery();
	}

	public GraphResult evaluate()
		throws StoreException
	{
		GraphResult result = getQuery().evaluate();
		return new GraphResultImpl(result.getNamespaces(), getSigner().sign(result));
	}

	public <H extends RDFHandler> H evaluate(H handler)
		throws StoreException, RDFHandlerException
	{
		getQuery().evaluate(new RDFHandlerWrapper(handler) {

			@Override
			public void handleStatement(Statement st)
				throws RDFHandlerException
			{
				st = getSigner().sign(st);
				super.handleStatement(st);
			}
		});

		return handler;
	}
}
