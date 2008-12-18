/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.members;

import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
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
public class MemberGraphQuery extends MemberQuery implements GraphQuery {

	private GraphQuery query;

	private Set<BNode> contains;

	public MemberGraphQuery(GraphQuery query, Map<BNode, BNode> in, Map<BNode, BNode> out, Set<BNode> contains)
	{
		super(query, in, out);
		this.query = query;
		this.contains = contains;
	}

	public GraphResult evaluate()
		throws StoreException
	{
		final GraphResult result = query.evaluate();
		return new GraphResultImpl(result.getNamespaces(), new MemberModelResult(result, out, contains));
	}

	public void evaluate(final RDFHandler handler)
		throws StoreException, RDFHandlerException
	{
		final MemberModelResult result = new MemberModelResult(out, contains);
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
				handler.handleStatement(result.export(st));
			}

			public void startRDF()
				throws RDFHandlerException
			{
				handler.startRDF();
			}
		});
	}

}
