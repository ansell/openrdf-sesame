/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;
import java.net.URL;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class SummaryServlet extends TransformationServlet {

	@Override
	public void service(PrintWriter out, String xslPath)
			throws StoreException, QueryEvaluationException,
			MalformedQueryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "summary.xsl");
		builder.start("id", "description", "location", "server", "size",
				"namespaces", "contexts");
		builder.link("info");
		String id = info.getId();
		String desc = info.getDescription();
		URL loc = info.getLocation();
		String server = getServer();
		RepositoryConnection con = repository.getConnection();
		try {
			long size = getSize(con);
			long namespaces = getNamespaces(con);
			long contexts = getContexts(con);
			builder.result(id, desc, loc, server, size, namespaces, contexts);
			builder.end();
		} finally {
			con.close();
		}
	}

	private String getServer() {
		if (manager instanceof LocalRepositoryManager) {
			return ((LocalRepositoryManager) manager).getBaseDir().toString();
		} else if (manager instanceof RemoteRepositoryManager) {
			return ((RemoteRepositoryManager) manager).getServerURL();
		}
		return null;
	}

	private long getSize(RepositoryConnection con) throws StoreException {
		return con.size();
	}

	private long getNamespaces(RepositoryConnection con)
			throws StoreException {
		return con.getNamespaces().asList().size();
	}

	private long getContexts(RepositoryConnection con)
			throws StoreException {
		return con.getContextIDs().asList().size();
	}

}
