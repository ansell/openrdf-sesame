package org.openrdf.workbench.commands;

import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TupleServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class ContextsServlet extends TupleServlet {

	public ContextsServlet() {
		super("contexts.xsl", "context");
	}

	@Override
	protected void service(TupleResultBuilder builder, RepositoryConnection con)
			throws RepositoryException {
		for (Resource ctx : con.getContextIDs().asList()) {
			builder.result(ctx);
		}
	}

}