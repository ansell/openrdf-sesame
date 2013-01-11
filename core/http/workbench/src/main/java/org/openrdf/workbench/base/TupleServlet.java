/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.base;

import java.io.BufferedWriter;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Namespace;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public abstract class TupleServlet extends TransformationServlet {
	protected String xsl;
	protected String[] variables;

	public TupleServlet(String xsl, String... variables) {
		this.xsl = xsl;
		this.variables = variables;
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		resp.setContentType("application/xml");
		PrintWriter writer = new PrintWriter(
				new BufferedWriter(resp.getWriter()));
		TupleResultBuilder builder = new TupleResultBuilder(writer);
		if (xsl != null) {
			builder.transform(xslPath, xsl);
		}
		RepositoryConnection con = repository.getConnection();
		try {
			for (Namespace ns : Iterations.asList(con.getNamespaces())) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			builder.start(variables);
			builder.link("info");
			this.service(req, resp, builder, con);
			builder.end();
			writer.flush();
		} finally {
			con.close();
		}
	}

	protected void service(WorkbenchRequest req, HttpServletResponse resp,
			TupleResultBuilder builder, RepositoryConnection con) 
					throws Exception {
		service(builder, con);
	}

	protected void service(TupleResultBuilder builder, 
			RepositoryConnection con)
			throws Exception {
	}
}