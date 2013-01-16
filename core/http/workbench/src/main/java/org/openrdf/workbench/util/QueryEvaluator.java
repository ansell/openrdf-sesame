/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import static org.openrdf.rio.RDFWriterRegistry.getInstance;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.exceptions.BadRequestException;

/**
 * Evaluates queries for QueryServlet.
 */
public final class QueryEvaluator {

	private static final String INFO = "info";

	public static final QueryEvaluator INSTANCE = new QueryEvaluator();

	private static final String ACCEPT = "Accept";

	private QueryEvaluator() {
		// do nothing
	}

	/**
	 * Evaluates the query submitted with the given request.
	 * 
	 * @param builder
	 *        used to build the response
	 * @param resp
	 *        the response object
	 * @param out
	 *        the output writer
	 * @param xslPath
	 *        style sheet path
	 * @param con
	 *        connection to repository
	 * @param req
	 *        the request object
	 * @param cookies
	 *        used to deal with browser cookies
	 * @throws BadRequestException
	 *         if there's a problem getting request parameters or issuing the
	 *         repository query
	 * @throws OpenRDFException
	 *         if there's a problem preparing the query
	 */
	public void extractQueryAndEvaluate(final TupleResultBuilder builder, final HttpServletResponse resp,
			final PrintWriter out, final String xslPath, final RepositoryConnection con,
			final WorkbenchRequest req, final CookieHandler cookies)
		throws BadRequestException, OpenRDFException
	{
		final QueryLanguage queryLn = QueryLanguage.valueOf(req.getParameter("queryLn"));
		String queryText = req.getParameter("query");
		Query query = QueryFactory.prepareQuery(con, queryLn, queryText);
		if (query instanceof GraphQuery || query instanceof TupleQuery) {
			final int know_total = req.getInt("know_total");
			if (know_total > 0) {
				cookies.addTotalResultCountCookie(req, resp, know_total);
			}
			else {
				final int result_count = (query instanceof GraphQuery) ? this.countQueryResults((GraphQuery)query)
						: this.countQueryResults((TupleQuery)query);
				cookies.addTotalResultCountCookie(req, resp, result_count);
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
		this.evaluate(builder, out, xslPath, req, query);
	}

	/***
	 * Evaluate a tuple query, and create an XML results document.
	 * 
	 * @param builder
	 *        response builder helper for generating the XML response to the
	 *        client
	 * @param query
	 *        the query to be evaluated
	 */
	public void evaluateTupleQuery(final TupleResultBuilder builder, final TupleQuery query)
		throws QueryEvaluationException
	{
		final TupleQueryResult result = query.evaluate();
		try {
			final String[] names = result.getBindingNames().toArray(new String[0]);
			builder.variables(names);
			builder.link(INFO);
			final List<Object> values = new ArrayList<Object>();
			while (result.hasNext()) {
				final BindingSet set = result.next();
				values.clear();
				for (int i = 0; i < names.length; i++) {
					values.add(set.getValue(names[i]));
				}
				builder.result(values.toArray());
			}
		}
		finally {
			result.close();
		}
	}

	/***
	 * Evaluate a graph query, and create an XML results document.
	 * 
	 * @param builder
	 *        response builder helper for generating the XML response to the
	 *        client
	 * @param query
	 *        the query to be evaluated
	 */
	private void evaluateGraphQuery(final TupleResultBuilder builder, final GraphQuery query)
		throws QueryEvaluationException
	{
		final GraphQueryResult result = query.evaluate();
		try {
			builder.variables("subject", "predicate", "object");
			builder.link(INFO);
			while (result.hasNext()) {
				final Statement statement = result.next();
				builder.result(statement.getSubject(), statement.getPredicate(), statement.getObject(),
						statement.getContext());
			}
		}
		finally {
			result.close();
		}
	}

	private int countQueryResults(final GraphQuery query)
		throws QueryEvaluationException
	{
		int rval = 0;
		final GraphQueryResult result = query.evaluate();
		try {
			while (result.hasNext()) {
				result.next();
				rval++;
			}
		}
		finally {
			result.close();
		}

		return rval;
	}

	private int countQueryResults(final TupleQuery query)
		throws QueryEvaluationException
	{
		int rval = 0;
		final TupleQueryResult result = query.evaluate();
		try {
			while (result.hasNext()) {
				result.next();
				rval++;
			}
		}
		finally {
			result.close();
		}

		return rval;
	}

	private void evaluateGraphQuery(final RDFWriter writer, final GraphQuery query)
		throws QueryEvaluationException, RDFHandlerException
	{
		query.evaluate(writer);
	}

	private void evaluateBooleanQuery(final TupleResultBuilder builder, final BooleanQuery query)
		throws QueryEvaluationException
	{
		final boolean result = query.evaluate();
		builder.link(INFO);
		builder.bool(result);
	}

	private void evaluate(final TupleResultBuilder builder, final PrintWriter out, final String xslPath,
			final WorkbenchRequest req, final Query query)
		throws OpenRDFException, BadRequestException
	{
		if (query instanceof TupleQuery) {
			builder.transform(xslPath, "tuple.xsl");
			builder.start();
			this.evaluateTupleQuery(builder, (TupleQuery)query);
			builder.end();
		}
		else {
			final RDFFormat format = req.isParameterPresent(ACCEPT) ? RDFFormat.forMIMEType(req.getParameter(ACCEPT))
					: null;
			if (query instanceof GraphQuery && format == null) {
				builder.transform(xslPath, "graph.xsl");
				builder.start();
				this.evaluateGraphQuery(builder, (GraphQuery)query);
				builder.end();
			}
			else if (query instanceof GraphQuery) {
				final RDFWriterFactory factory = getInstance().get(format);
				final RDFWriter writer = factory.getWriter(out);
				this.evaluateGraphQuery(writer, (GraphQuery)query);
			}
			else if (query instanceof BooleanQuery) {
				builder.transform(xslPath, "boolean.xsl");
				builder.start();
				this.evaluateBooleanQuery(builder, (BooleanQuery)query);
				builder.endBoolean();
			}
			else {
				throw new BadRequestException("Unknown query type: " + query.getClass().getSimpleName());
			}
		}
	}

}
