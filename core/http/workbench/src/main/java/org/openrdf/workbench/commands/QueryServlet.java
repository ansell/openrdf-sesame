/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.commands;

import static org.openrdf.rio.RDFWriterRegistry.getInstance;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.PagedQuery;
import org.openrdf.workbench.util.QueryEvaluator;
import org.openrdf.workbench.util.QueryFactory;
import org.openrdf.workbench.util.QueryStorage;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class QueryServlet extends TransformationServlet {

	private static final String INFO = "info";

	private static final String ACCEPT = "Accept";

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final QueryEvaluator EVAL = QueryEvaluator.INSTANCE;

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "total_result_count" };
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
		throws IOException, BadRequestException, MalformedURLException, OpenRDFException, JSONException
	{
		resp.setContentType("application/json");
		final PrintWriter out = resp.getWriter();
		final PrintWriter writer = new PrintWriter(new BufferedWriter(out));
		if (!"save".equals(req.getParameter("action"))) {
			throw new BadRequestException("Query doPost() is only for 'action=save'.");
		}
		final JSONObject json = new JSONObject();
		final QueryStorage storage = new QueryStorage(this.getServletConfig().getServletContext());
		final URL repository = this.manager.getLocation();
		final String userName = req.getParameter(SERVER_USER);
		final String password = req.getParameter(SERVER_PASSWORD);
		final boolean accessible = storage.checkAccess(repository, userName, password);
		json.put("accessible", accessible);
		boolean exists = false;
		if (accessible) {
			final String queryName = req.getParameter("query-name");
			exists = storage.askExists(repository, queryName, userName);
			if (!exists) {
				final boolean shared = Boolean.valueOf(req.getParameter("save-private"));
				final QueryLanguage queryLanguage = QueryLanguage.valueOf(req.getParameter("queryLn"));
				final String queryText = req.getParameter("query");
				final int rowsPerPage = Integer.valueOf(req.getParameter("limit"));
				storage.saveQuery(repository, queryName, userName, shared, queryLanguage, queryText, rowsPerPage);
			}
		}
		json.put("existed", exists);
		json.put("written", !exists);
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
			if (req.isParameterPresent("query")) {
				try {
					service(builder, resp, out, xslPath, con, req);
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

	private void service(final TupleResultBuilder builder, final HttpServletResponse resp,
			final PrintWriter out, final String xslPath, final RepositoryConnection con,
			final WorkbenchRequest req)
		throws BadRequestException, OpenRDFException
	{
		final QueryLanguage queryLn = QueryLanguage.valueOf(req.getParameter("queryLn"));
		String queryText = req.getParameter("query");
		Query query = QueryFactory.prepareQuery(con, queryLn, queryText);
		if (query instanceof GraphQuery || query instanceof TupleQuery) {
			final int know_total = req.getInt("know_total");
			if (know_total > 0) {
				addTotalResultCountCookie(req, resp, know_total);
			}
			else {
				final int result_count = (query instanceof GraphQuery) ? EVAL.countQueryResults((GraphQuery)query)
						: EVAL.countQueryResults((TupleQuery)query);
				addTotalResultCountCookie(req, resp, result_count);
			}
			final int limit = req.getInt("limit");
			final int offset = req.getInt("offset");
			final PagedQuery pagedQuery = new PagedQuery(queryText, queryLn, limit, offset);
			queryText = pagedQuery.toString();
			query = QueryFactory.prepareQuery(con, queryLn, queryText);
		}
		if (req.isParameterPresent("infer")) {
			final boolean infer = Boolean.parseBoolean(req.getParameter("infer"));
			query.setIncludeInferred(infer);
		}
		service(builder, out, xslPath, req, query);
	}

	private void service(final TupleResultBuilder builder, final PrintWriter out, final String xslPath,
			final WorkbenchRequest req, final Query query)
		throws OpenRDFException, BadRequestException
	{
		if (query instanceof TupleQuery) {
			builder.transform(xslPath, "tuple.xsl");
			builder.start();
			EVAL.evaluateTupleQuery(builder, (TupleQuery)query);
			builder.end();
		}
		else {
			final RDFFormat format = req.isParameterPresent(ACCEPT) ? RDFFormat.forMIMEType(req.getParameter(ACCEPT))
					: null;
			if (query instanceof GraphQuery && format == null) {
				builder.transform(xslPath, "graph.xsl");
				builder.start();
				EVAL.evaluateGraphQuery(builder, (GraphQuery)query);
				builder.end();
			}
			else if (query instanceof GraphQuery) {
				final RDFWriterFactory factory = getInstance().get(format);
				final RDFWriter writer = factory.getWriter(out);
				EVAL.evaluateGraphQuery(writer, (GraphQuery)query);
			}
			else if (query instanceof BooleanQuery) {
				builder.transform(xslPath, "boolean.xsl");
				builder.start();
				EVAL.evaluateBooleanQuery(builder, (BooleanQuery)query);
				builder.end();
			}
			else {
				throw new BadRequestException("Unknown query type: " + query.getClass().getSimpleName());
			}
		}
	}
}