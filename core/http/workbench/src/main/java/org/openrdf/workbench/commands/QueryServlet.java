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

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Namespace;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.QueryEvaluator;
import org.openrdf.workbench.util.PagedQuery;
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
		throws IOException, RepositoryException, RDFHandlerException, QueryEvaluationException
	{
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

	private void service(final WorkbenchRequest req, final HttpServletResponse resp, final PrintWriter out,
			final String xslPath)
		throws BadRequestException, RepositoryException, RDFHandlerException, QueryEvaluationException
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
		throws BadRequestException, MalformedQueryException, RepositoryException, QueryEvaluationException,
		RDFHandlerException
	{
		final QueryLanguage queryLn = QueryLanguage.valueOf(req.getParameter("queryLn"));
		final int limit = req.getInt("limit");
		final int offset = req.getInt("offset");
		String queryText = req.getParameter("query");
		Query query = prepareQuery(con, queryLn, queryText);
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
			final PagedQuery pagedQuery = new PagedQuery(queryText, queryLn, limit, offset);
			queryText = pagedQuery.toString();
			query = prepareQuery(con, queryLn, queryText);
		}
		if (req.isParameterPresent("infer")) {
			final boolean infer = Boolean.parseBoolean(req.getParameter("infer"));
			query.setIncludeInferred(infer);
		}
		service(builder, out, xslPath, req, query);
	}

	/**
	 * @param builder
	 * @param out
	 * @param xslPath
	 * @param req
	 * @param query
	 * @throws QueryEvaluationException
	 * @throws RDFHandlerException
	 * @throws BadRequestException
	 */
	private void service(final TupleResultBuilder builder, final PrintWriter out, final String xslPath,
			final WorkbenchRequest req, final Query query)
		throws QueryEvaluationException, RDFHandlerException, BadRequestException
	{
		final RDFFormat format = req.isParameterPresent(ACCEPT) ? RDFFormat.forMIMEType(req.getParameter(ACCEPT))
				: null;
		if (query instanceof TupleQuery) {
			builder.transform(xslPath, "tuple.xsl");
			builder.start();
			EVAL.evaluateTupleQuery(builder, (TupleQuery)query);
			builder.end();
		}
		else if (query instanceof GraphQuery && format == null) {
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

	private Query prepareQuery(final RepositoryConnection con, final QueryLanguage queryLn, final String query)
		throws RepositoryException, MalformedQueryException
	{
		try {
			return con.prepareQuery(queryLn, query);
		}
		catch (UnsupportedOperationException exc) {
			// TODO must be an http repository
			try {
				con.prepareTupleQuery(queryLn, query).evaluate().close();
				return con.prepareTupleQuery(queryLn, query);
			}
			catch (Exception malformed) {
				// guess its not a tuple query
			}
			try {
				con.prepareGraphQuery(queryLn, query).evaluate().close();
				return con.prepareGraphQuery(queryLn, query);
			}
			catch (Exception malformed) {
				// guess its not a graph query
			}
			try {
				con.prepareBooleanQuery(queryLn, query).evaluate();
				return con.prepareBooleanQuery(queryLn, query);
			}
			catch (Exception malformed) {
				// guess its not a boolean query
			}
			// let's assume it is an malformed tuple query
			return con.prepareTupleQuery(queryLn, query);
		}
	}
}