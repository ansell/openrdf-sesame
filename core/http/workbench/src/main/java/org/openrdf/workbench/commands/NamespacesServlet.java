/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Namespace;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class NamespacesServlet extends TransformationServlet {

	@Override
	protected void doPost(WorkbenchRequest req, HttpServletResponse resp,
			String xslPath) throws Exception {
		RepositoryConnection con = repository.getConnection();
		try {
			String prefix = req.getParameter("prefix");
			String namespace = req.getParameter("namespace");
			if (namespace.length() > 0) {
				con.setNamespace(prefix, namespace);
			} else {
				con.removeNamespace(prefix);
			}
		} finally {
			con.close();
		}
		super.service(req, resp, xslPath);
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws RepositoryException {
		TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "namespaces.xsl");
		RepositoryConnection con = repository.getConnection();
		try {
			builder.start("prefix", "namespace");
			builder.link("info");
			for (Namespace ns : Iterations.asList(con.getNamespaces())) {
				builder.result(ns.getPrefix(), ns.getName());
			}
			builder.end();
		} finally {
			con.close();
		}
	}

}
