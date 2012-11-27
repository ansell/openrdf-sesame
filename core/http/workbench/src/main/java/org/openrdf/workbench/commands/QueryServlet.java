/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryEvaluator;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class QueryServlet extends TransformationServlet {

	private static final String ACCEPT = "Accept";

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final QueryEvaluator EVAL = QueryEvaluator.INSTANCE;

	private QueryStorage storage;

	/**
	 * @return the names of the cookies that will be retrieved from the request,
	 *         and returned in the response
	 */
	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "total_result_count" };
	}

	/**
	 * Initialize this instance of the servlet.
	 * 
	 * @param config configuration passed in by the application container
	 */
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
		throws IOException, OpenRDFException
	{
		setContentType(req, resp);
		final PrintWriter out = resp.getWriter();
		try {
			final PrintWriter writer = new PrintWriter(new BufferedWriter(out));
			service(req, resp, writer, xslPath);
			writer.flush();
		}
		catch (BadRequestException exc) {
			LOGGER.warn(exc.toString(), exc);
			resp.setContentType("application/xml");
			final TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "query.xsl");
			builder.start("error-message");
			builder.link(INFO);
			builder.link("namespaces");
			builder.result(exc.getMessage());
			builder.end();
		}
	}

	@Override
	protected void doPost(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, BadRequestException, OpenRDFException, JSONException
	{
		resp.setContentType("application/json");
		if (!"save".equals(req.getParameter("action"))) {
			throw new BadRequestException("Query doPost() is only for 'action=save'.");
		}
		final JSONObject json = new JSONObject();
		final boolean accessible = storage.checkAccess((HTTPRepository)this.repository);
		json.put("accessible", accessible);
		if (accessible) {
			final HTTPRepository http = (HTTPRepository)repository;
			final String queryName = req.getParameter("query-name");
			String userName = req.getParameter(SERVER_USER);
			if (null == userName) {
				userName = "";
			}
			final boolean existed = storage.askExists(http, queryName, userName);
			json.put("existed", existed);
			final boolean written = Boolean.valueOf(req.getParameter("overwrite")) || !existed;
			final boolean shared = !Boolean.valueOf(req.getParameter("save-private"));
			final QueryLanguage queryLanguage = QueryLanguage.valueOf(req.getParameter("queryLn"));
			final String queryText = req.getParameter("query");
			final boolean infer = Boolean.valueOf(req.getParameter("infer"));
			final int rowsPerPage = Integer.valueOf(req.getParameter("limit"));
			if (written) {
				if (existed) {
					final URI query = storage.selectSavedQuery(http, userName, queryName);
					storage.updateQuery(query, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
				}
				else {
					storage.saveQuery(http, queryName, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
				}
			}
			json.put("written", written);
		}
		final PrintWriter writer = new PrintWriter(new BufferedWriter(resp.getWriter()));
		writer.write(json.toString());
		writer.flush();
	}

	private void setContentType(final WorkbenchRequest req, final HttpServletResponse resp) {
		if (req.isParameterPresent(ACCEPT)) {
			final String accept = req.getParameter(ACCEPT);
			final RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				resp.setContentType(accept);
				final String ext = format.getDefaultFileExtension();
				final String attachment = "attachment; filename=query." + ext;
				resp.setHeader("Content-disposition", attachment);
			}
		}
		else {
			resp.setContentType("application/xml");
		}
	}

	private void service(final WorkbenchRequest req, final HttpServletResponse resp, final PrintWriter out,
			final String xslPath)
		throws BadRequestException, OpenRDFException
	{
		final RepositoryConnection con = repository.getConnection();
		try {
			final TupleResultBuilder builder = new TupleResultBuilder(out);
			for (Namespace ns : con.getNamespaces().asList()) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			final String action = req.getParameter("action");
			if (req.isParameterPresent("query") && !"edit".equals(action)) {
				try {
					EVAL.extractQueryAndEvaluate(builder, resp, out, xslPath, con, req, this.cookies);
				}
				catch (MalformedQueryException exc) {
					throw new BadRequestException(exc.getMessage(), exc);
				}
				catch (HTTPQueryEvaluationException exc) {
					if (exc.getCause() instanceof MalformedQueryException) {
						throw new BadRequestException(exc.getCause().getMessage(), exc);
					}
					throw exc;
				}
			}
			else {
				builder.transform(xslPath, "query.xsl");
				builder.start();
				builder.link(INFO);
				builder.link("namespaces");
				builder.end();
			}
		}
		finally {
			con.close();
		}
	}
}