/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.workbench.util;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import info.aduna.iteration.Iterations;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterRegistry;
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
	 * @param queryText
	 *        the query text, having been pulled using
	 *        {@link org.openrdf.workbench.commands.QueryServlet} from one of
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
		boolean deferResultCount = false;
		if (query instanceof GraphQuery || query instanceof TupleQuery) {
			final int limit = req.getInt("limit");
			deferResultCount = limit <= 0;
			final int know_total = req.getInt("know_total");
			if (know_total > 0) {
				cookies.addTotalResultCountCookie(req, resp, know_total);
			}
			else {
				if (!deferResultCount) {
					// When all results are wanted, we skip initial this round trip
					// to the server just to get a count, preferring to just pull in
					// all results at once.
					// TODO Skip this when limit has been set > 0, too.
					final int result_count = (query instanceof GraphQuery) ? this.countQueryResults((GraphQuery)query)
							: this.countQueryResults((TupleQuery)query);
					cookies.addTotalResultCountCookie(req, resp, result_count);
				}
			}
			if (!deferResultCount) {
				query = QueryFactory.prepareQuery(con, queryLn,
						new PagedQuery(queryText, queryLn, limit, req.getInt("offset")).toString());
			}
		}
		if (req.isParameterPresent("infer")) {
			final boolean infer = Boolean.parseBoolean(req.getParameter("infer"));
			query.setIncludeInferred(infer);
		}
		// TODO use limit, offset and know_total to skip the first evaluation for
		// counting above when possible.
		this.evaluate(builder, out, xslPath, req, resp, cookies, query, deferResultCount);
	}

	/***
	 * Evaluate a tuple query, and create an XML results document. This method
	 * completes writing of the response.
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
	 * @throws QueryResultHandlerException
	 */
	public void evaluateTupleQuery(final TupleResultBuilder builder, String xslPath, WorkbenchRequest req,
			HttpServletResponse resp, CookieHandler cookies, final TupleQuery query, boolean deferResultCount)
		throws QueryEvaluationException, QueryResultHandlerException
	{
		final TupleQueryResult result = query.evaluate();
		final String[] names = result.getBindingNames().toArray(new String[0]);
		List<BindingSet> bindings = Iterations.asList(result);
		if (deferResultCount) {
			cookies.addTotalResultCountCookie(req, resp, bindings.size());
		}
		builder.transform(xslPath, "tuple.xsl");
		builder.start();
		builder.variables(names);
		builder.link(Arrays.asList(INFO));
		final List<Object> values = new ArrayList<Object>(names.length);
		for (BindingSet set : bindings) {
			values.clear();
			for (int i = 0; i < names.length; i++) {
				values.add(set.getValue(names[i]));
			}
			builder.result(values.toArray());
		}
		builder.end();
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
	 * @throws QueryResultHandlerException
	 */
	private void evaluateGraphQuery(final TupleResultBuilder builder, String xslPath, WorkbenchRequest req,
			HttpServletResponse resp, CookieHandler cookies, final GraphQuery query, boolean deferResultCount)
		throws QueryEvaluationException, QueryResultHandlerException
	{
		List<Statement> statements = Iterations.asList(query.evaluate());
		if (deferResultCount) {
			cookies.addTotalResultCountCookie(req, resp, statements.size());
		}
		builder.transform(xslPath, "graph.xsl");
		builder.start();
		builder.variables("subject", "predicate", "object");
		builder.link(Arrays.asList(INFO));
		for (Statement statement : statements) {
			builder.result(statement.getSubject(), statement.getPredicate(), statement.getObject(),
					statement.getContext());
		}
		builder.end();
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
		throws QueryEvaluationException, QueryResultHandlerException
	{
		final boolean result = query.evaluate();
		builder.link(Arrays.asList(INFO));
		builder.bool(result);
	}

	private void evaluate(final TupleResultBuilder builder, final OutputStream out, final String xslPath,
			final WorkbenchRequest req, HttpServletResponse resp, CookieHandler cookies, final Query query,
			boolean deferResultCount)
		throws OpenRDFException, BadRequestException
	{
		if (query instanceof TupleQuery) {
			this.evaluateTupleQuery(builder, xslPath, req, resp, cookies, (TupleQuery)query, deferResultCount);
		}
		else {
			final RDFFormat format = req.isParameterPresent(ACCEPT) ? RDFFormat.forMIMEType(req.getParameter(ACCEPT))
					: null;
			if (query instanceof GraphQuery) {
				GraphQuery graphQuery = (GraphQuery)query;
				if (null == format) {
					this.evaluateGraphQuery(builder, xslPath, req, resp, cookies, graphQuery, deferResultCount);
				}
				else {
					this.evaluateGraphQuery(RDFWriterRegistry.getInstance().get(format).getWriter(out), graphQuery);
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
