/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.openrdf.OpenRDFException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;

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
	protected void service(final PrintWriter out, final String xslPath)
		throws BadRequestException, OpenRDFException
	{
		final TupleResultBuilder builder = new TupleResultBuilder(out);
		builder.transform(xslPath, "saved-queries.xsl");
		builder.start();
		builder.link(INFO);
		builder.end();
	}
}
