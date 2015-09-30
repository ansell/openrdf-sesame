/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.workbench.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.OpenRDFException;
import org.eclipse.rdf4j.common.iteration.Iterations;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.BooleanQuery;
import org.eclipse.rdf4j.query.GraphQuery;
import org.eclipse.rdf4j.query.Query;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.RDFWriter;
import org.eclipse.rdf4j.rio.RDFWriterRegistry;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.workbench.exceptions.BadRequestException;

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
	 * @param queryText
	 *        the query text, having been pulled using
	 *        {@link org.eclipse.rdf4j.workbench.commands.QueryServlet} from one of
	 *        three request parameters: "query", "queryhash" or "saved"
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
			final OutputStream out, final String xslPath, final RepositoryConnection con, String queryText,
			final WorkbenchRequest req, final CookieHandler cookies)
		throws BadRequestException, OpenRDFException
	{
		final QueryLanguage queryLn = QueryLanguage.valueOf(req.getParameter("queryLn"));
		Query query = QueryFactory.prepareQuery(con, queryLn, queryText);
		boolean evaluateCookie = false;
		int offset = req.getInt("offset");
		int limit = req.getInt("limit_query");
		boolean paged = limit > 0;
		if (query instanceof GraphQuery || query instanceof TupleQuery) {
			final int know_total = req.getInt("know_total");
			evaluateCookie = know_total <= 0;
			if (!evaluateCookie) {
				cookies.addTotalResultCountCookie(req, resp, know_total);
			}
			if (paged) {
				PagedQuery pagedQuery = new PagedQuery(queryText, queryLn, limit, offset);
				if (pagedQuery.isPaged()) {
					offset = pagedQuery.getOffset();
					limit = pagedQuery.getLimit();
				}
				if (!evaluateCookie) {
					query = QueryFactory.prepareQuery(con, queryLn, pagedQuery.toString());
				}
			}
		}
		if (req.isParameterPresent("infer")) {
			final boolean infer = Boolean.parseBoolean(req.getParameter("infer"));
			query.setIncludeInferred(infer);
		}
		this.evaluate(builder, out, xslPath, req, resp, cookies, query, evaluateCookie, paged, offset, limit);
	}

	/***
	 * Evaluate a tuple query, and create an XML results document. This method
	 * completes writing of the response. !paged means use all results.
	 * 
	 * @param builder
	 *        response builder helper for generating the XML response to the
	 *        client, which <em>must not</em> have had start() called on it
	 * @param xslPath
	 *        needed to begin writing response body after writing result count
	 *        cookie
	 * @param req
	 *        needed to write result count cookie
	 * @param resp
	 *        needed to write result count cookie
	 * @param cookies
	 *        needed to write result count cookie
	 * @param query
	 *        the query to be evaluated
	 * @param writeCookie
	 *        whether to write the total result count cookie
	 * @param paged
	 *        whether to display a limited subset
	 * @throws QueryResultHandlerException
	 */
	public void evaluateTupleQuery(final TupleResultBuilder builder, String xslPath, WorkbenchRequest req,
			HttpServletResponse resp, CookieHandler cookies, final TupleQuery query, boolean writeCookie,
			boolean paged, int offset, int limit)
		throws QueryEvaluationException, QueryResultHandlerException
	{
		final TupleQueryResult result = query.evaluate();
		final String[] names = result.getBindingNames().toArray(new String[0]);
		List<BindingSet> bindings = Iterations.asList(result);
		if (writeCookie) {
			cookies.addTotalResultCountCookie(req, resp, bindings.size());
		}
		builder.transform(xslPath, "tuple.xsl");
		builder.start();
		builder.variables(names);
		builder.link(Arrays.asList(INFO));
		final List<Object> values = new ArrayList<Object>(names.length);
		if (paged && writeCookie) {
			// Only in this case do we have paged results, but were given the full
			// query. Just-in-case parameter massaging below to avoid array index
			// issues.
			int fromIndex = Math.min(0, offset);
			bindings = bindings.subList(fromIndex,
					Math.max(fromIndex, Math.min(offset + limit, bindings.size())));
		}
		for (BindingSet set : bindings) {
			addResult(builder, names, values, set);
		}
		builder.end();
	}

	private void addResult(final TupleResultBuilder builder, final String[] names, final List<Object> values,
			BindingSet set)
		throws QueryResultHandlerException
	{
		values.clear();
		for (int i = 0; i < names.length; i++) {
			values.add(set.getValue(names[i]));
		}
		builder.result(values.toArray());
	}

	/***
	 * Evaluate a tuple query, and create an XML results document. It is still
	 * necessary to call end() on the builder after calling this method.
	 * 
	 * @param builder
	 *        response builder helper for generating the XML response to the
	 *        client, which <em>must</em> have had start() called on it
	 * @param query
	 *        the query to be evaluated
	 * @throws QueryResultHandlerException
	 */
	public void evaluateTupleQuery(final TupleResultBuilder builder, final TupleQuery query)
		throws QueryEvaluationException, QueryResultHandlerException
	{
		final TupleQueryResult result = query.evaluate();
		try {
			final String[] names = result.getBindingNames().toArray(new String[0]);
			builder.variables(names);
			builder.link(Arrays.asList(INFO));
			final List<Object> values = new ArrayList<Object>();
			while (result.hasNext()) {
				final BindingSet set = result.next();
				addResult(builder, names, values, set);
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
	 *        client, which <em>must not</em> have had start() called on it
	 * @param xslPath
	 *        needed to begin writing response body after writing result count
	 *        cookie
	 * @param req
	 *        needed to write result count cookie
	 * @param resp
	 *        needed to write result count cookie
	 * @param cookies
	 *        needed to write result count cookie
	 * @param query
	 *        the query to be evaluated
	 * @param writeCookie
	 *        whether to write the total result count cookie
	 * @throws QueryResultHandlerException
	 */
	private void evaluateGraphQuery(final TupleResultBuilder builder, String xslPath, WorkbenchRequest req,
			HttpServletResponse resp, CookieHandler cookies, final GraphQuery query, boolean writeCookie,
			boolean paged, int offset, int limit)
		throws QueryEvaluationException, QueryResultHandlerException
	{
		List<Statement> statements = Iterations.asList(query.evaluate());
		if (writeCookie) {
			cookies.addTotalResultCountCookie(req, resp, statements.size());
		}
		builder.transform(xslPath, "graph.xsl");
		builder.start();
		builder.variables("subject", "predicate", "object");
		builder.link(Arrays.asList(INFO));
		if (paged && writeCookie) {
			// Only in this case do we have paged results, but were given the full
			// query. Just-in-case parameter massaging below to avoid array index
			// issues.
			int fromIndex = Math.min(0, offset);
			statements = statements.subList(fromIndex,
					Math.max(fromIndex, Math.min(offset + limit, statements.size())));
		}
		for (Statement statement : statements) {
			builder.result(statement.getSubject(), statement.getPredicate(), statement.getObject(),
					statement.getContext());
		}
		builder.end();
	}

	private void evaluateGraphQuery(final RDFWriter writer, final GraphQuery query)
		throws QueryEvaluationException, RDFHandlerException
	{
		query.evaluate(writer);
	}

	private void evaluateBooleanQuery(final TupleResultBuilder builder, final BooleanQuery query)
		throws QueryEvaluationException, QueryResultHandlerException
	{
		final boolean result = query.evaluate();
		builder.link(Arrays.asList(INFO));
		builder.bool(result);
	}

	private void evaluate(final TupleResultBuilder builder, final OutputStream out, final String xslPath,
			final WorkbenchRequest req, HttpServletResponse resp, CookieHandler cookies, final Query query,
			boolean writeCookie, boolean paged, int offset, int limit)
		throws OpenRDFException, BadRequestException
	{
		if (query instanceof TupleQuery) {
			this.evaluateTupleQuery(builder, xslPath, req, resp, cookies, (TupleQuery)query, writeCookie, paged,
					offset, limit);
		}
		else {
			final RDFFormat format = req.isParameterPresent(ACCEPT) ? Rio.getWriterFormatForMIMEType(
					req.getParameter(ACCEPT)).orElse(null) : null;
			if (query instanceof GraphQuery) {
				GraphQuery graphQuery = (GraphQuery)query;
				if (null == format) {
					this.evaluateGraphQuery(builder, xslPath, req, resp, cookies, graphQuery, writeCookie, paged,
							offset, limit);
				}
				else {
					this.evaluateGraphQuery(Rio.createWriter(format, out), graphQuery);
				}
			}
			else if (query instanceof BooleanQuery) {
				builder.transform(xslPath, "boolean.xsl");
				builder.startBoolean();
				this.evaluateBooleanQuery(builder, (BooleanQuery)query);
				builder.endBoolean();
			}
			else {
				throw new BadRequestException("Unknown query type: " + query.getClass().getSimpleName());
			}
		}
	}

}
