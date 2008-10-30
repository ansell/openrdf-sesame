/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class ContextsServlet extends TupleServlet {

	public ContextsServlet() {
		super("contexts.xsl", "context");
	}

	@Override
	protected void service(TupleResultBuilder builder, RepositoryConnection con)
			throws StoreException {
		for (Resource ctx : con.getContextIDs().asList()) {
			builder.result(ctx);
		}
	}

}