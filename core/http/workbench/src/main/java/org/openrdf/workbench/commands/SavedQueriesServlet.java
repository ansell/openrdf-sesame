/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

/**
 * Servlet that provides a page to access saved queries.
 * 
 * @author Dale Visser
 */
public class SavedQueriesServlet extends TransformationServlet {

	private QueryStorage storage;

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "total_result_count" };
	}

	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		try {
			this.storage = QueryStorage.getSingletonInstance(config.getServletContext());
		}
		catch (RepositoryException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, OpenRDFException, BadRequestException
	{
		resp.setContentType("application/xml");
		final PrintWriter out = resp.getWriter();
		final TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "saved-queries.xsl");
		builder.start();
		builder.link(INFO);
		this.getSavedQueries(req, builder);
		builder.end();
	}

	private void getSavedQueries(final WorkbenchRequest req, final TupleResultBuilder builder)
		throws OpenRDFException, BadRequestException
	{
		final HTTPRepository repo = (HTTPRepository)this.repository;
		String user = req.getParameter(SERVER_USER);
		if (null == user){
			user = "";
		}
		if (!storage.checkAccess(repo)) {
			throw new BadRequestException("User '" + user + "' not authorized to access repository '"
					+ repo.getRepositoryURL() + "'");
		}
		storage.selectSavedQueries(repo, user, builder);
	}
}