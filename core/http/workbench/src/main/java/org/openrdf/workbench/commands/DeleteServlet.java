/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.repository.config.RepositoryConfigException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.store.StoreException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class DeleteServlet extends TransformationServlet {
	/**
	 * Query that yields the context of a specific repository configuration.
	 */
	public static final String CONTEXT_QUERY;

	static {
		StringBuilder query = new StringBuilder(256);
		query.append("SELECT C ");
		query.append("FROM CONTEXT C ");
		query.append("   {} rdf:type {sys:Repository};");
		query.append("      sys:repositoryID {ID} ");
		query
				.append("USING NAMESPACE sys = <http://www.openrdf.org/config/repository#>");
		CONTEXT_QUERY = query.toString();
	}

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		dropRepository(req.getParameter("id"));
		resp.sendRedirect("../");
	}

	private void dropRepository(String id) throws RepositoryConfigException, StoreException {
		manager.removeRepositoryConfig(id);
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws RepositoryConfigException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "delete.xsl");
		builder.start("readable", "writeable", "id", "description", "location");
		builder.link("info");
		for (RepositoryInfo info : manager.getAllRepositoryInfos()) {
			builder.result(info.isReadable(), info.isWritable(), info.getId(),
					info.getDescription(), info.getLocation());
		}
		builder.end();
	}

}
