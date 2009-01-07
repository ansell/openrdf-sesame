/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
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
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class SignedGraphQuery extends SignedQuery implements GraphQuery {

	private GraphQuery query;

	public SignedGraphQuery(GraphQuery query, BNodeSigner signer) {
		super(query, signer);
		this.query = query;
	}

	public GraphResult evaluate()
		throws StoreException
	{
		final GraphResult result = query.evaluate();
		return new GraphResultImpl(result.getNamespaces(), signer.sign(result));
	}

	public void evaluate(final RDFHandler handler)
		throws StoreException, RDFHandlerException
	{
		query.evaluate(new RDFHandler() {

			public void endRDF()
				throws RDFHandlerException
			{
				handler.endRDF();
			}

			public void handleComment(String comment)
				throws RDFHandlerException
			{
				handler.handleComment(comment);
			}

			public void handleNamespace(String prefix, String uri)
				throws RDFHandlerException
			{
				handler.handleNamespace(prefix, uri);
			}

			public void handleStatement(Statement st)
				throws RDFHandlerException
			{
				handler.handleStatement(signer.sign(st));
			}

			public void startRDF()
				throws RDFHandlerException
			{
				handler.startRDF();
			}
		});
	}

}
