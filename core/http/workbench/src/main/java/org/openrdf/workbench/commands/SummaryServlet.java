/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;
import java.net.URL;

import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.EvaluationException;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.LocalRepositoryManager;
import org.openrdf.repository.manager.RemoteRepositoryManager;
import org.openrdf.store.StoreException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;

public class SummaryServlet extends TransformationServlet {

	@Override
	public void service(PrintWriter out, String xslPath)
			throws StoreException, EvaluationException,
			MalformedQueryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "summary.xsl");
		builder.start("id", "description", "location", "server");
		builder.link("info");
		String id = info.getId();
		String desc = info.getDescription();
		URL loc = info.getLocation();
		String server = getServer();
		RepositoryConnection con = repository.getConnection();
		try {
			builder.result(id, desc, loc, server);
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
}
