/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import static org.openrdf.query.QueryLanguage.SERQL;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.StoreException;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
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

	private void dropRepository(String id) throws Exception {
		Repository systemRepo = manager.getSystemRepository();
		RepositoryConnection con = systemRepo.getConnection();
		try {
			Resource context = findContext(id, con);
			manager.getRepository(id).shutDown();
			con.clear(context);
		} finally {
			con.close();
		}
	}

	private Resource findContext(String id, RepositoryConnection con)
			throws StoreException, MalformedQueryException,
			EvaluationException, BadRequestException {
		TupleQuery query = con.prepareTupleQuery(SERQL, CONTEXT_QUERY);
		query.setBinding("ID", vf.createLiteral(id));
		TupleQueryResult result = query.evaluate();
		try {
			if (!result.hasNext())
				throw new BadRequestException("Cannot find repository of id: "
						+ id);
			BindingSet bindings = result.next();
			Resource context = (Resource) bindings.getValue("C");
			if (result.hasNext())
				throw new BadRequestException(
						"Multiple contexts found for repository '" + id + "'");
			return context;
		} finally {
			result.close();
		}
	}

	@Override
	public void service(PrintWriter out, String xslPath)
			throws StoreException {
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
