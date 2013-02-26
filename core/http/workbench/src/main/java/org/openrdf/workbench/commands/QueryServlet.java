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
package org.openrdf.workbench.commands;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.Arrays;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.aduna.iteration.Iterations;

import org.openrdf.OpenRDFException;
import org.openrdf.model.Namespace;
import org.openrdf.model.URI;
import org.openrdf.model.impl.IntegerLiteralImpl;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.UnsupportedQueryResultFormatException;
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

	private static final String QUERY = "query";

	private static final String[] EDIT_PARAMS = new String[] { "queryLn", QUERY, "infer", "limit" };

	private static final Logger LOGGER = LoggerFactory.getLogger(QueryServlet.class);

	private static final QueryEvaluator EVAL = QueryEvaluator.INSTANCE;

	private QueryStorage storage;

	/**
	 * @return the names of the cookies that will be retrieved from the request,
	 *         and returned in the response
	 */
	@Override
	public String[] getCookieNames() {
		return new String[] { QUERY, "limit", "queryLn", "infer", "total_result_count", "show-datatypes" };
	}

	/**
	 * Initialize this instance of the servlet.
	 * 
	 * @param config
	 *        configuration passed in by the application container
	 */
	@Override
	public void init(final ServletConfig config)
		throws ServletException
	{
		super.init(config);
		try {
			this.storage = QueryStorage.getSingletonInstance(this.appConfig);
		}
		catch (RepositoryException e) {
			throw new ServletException(e);
		}
		catch (IOException e) {
			throw new ServletException(e);
		}
	}

	@Override
	protected void service(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, OpenRDFException
	{
		setContentType(req, resp);
		final OutputStream out = resp.getOutputStream();
		try {
			service(req, resp, out, xslPath);
		}
		catch (BadRequestException exc) {
			LOGGER.warn(exc.toString(), exc);
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, out);
			builder.transform(xslPath, "query.xsl");
			builder.start("error-message");
			builder.link(Arrays.asList(INFO, "namespaces"));
			builder.result(exc.getMessage());
			builder.end();
		}
		finally {
			out.flush();
		}
	}

	@Override
	protected void doPost(final WorkbenchRequest req, final HttpServletResponse resp, final String xslPath)
		throws IOException, BadRequestException, OpenRDFException, JSONException
	{
		final String action = req.getParameter("action");
		if ("save".equals(action)) {
			saveQuery(req, resp);
		}
		else if ("edit".equals(action)) {
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			builder.transform(xslPath, "query.xsl");
			builder.start(EDIT_PARAMS);
			builder.link(Arrays.asList(INFO, "namespaces"));
			final String queryLn = req.getParameter(EDIT_PARAMS[0]);
			final String query = req.getParameter(EDIT_PARAMS[1]);
			final Boolean infer = Boolean.valueOf(req.getParameter(EDIT_PARAMS[2]));
			final IntegerLiteralImpl limit = new IntegerLiteralImpl(new BigInteger(
					req.getParameter(EDIT_PARAMS[3])));
			builder.result(queryLn, query, infer, limit);
			builder.end();
		}
		else {
			throw new BadRequestException("Query doPost() is only for 'action=save' or 'action=edit'.");
		}
	}

	private void saveQuery(final WorkbenchRequest req, final HttpServletResponse resp)
		throws IOException, BadRequestException, OpenRDFException, JSONException
	{
		resp.setContentType("application/json");
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
			final String queryText = req.getParameter(QUERY);
			final boolean infer = Boolean.valueOf(req.getParameter("infer"));
			final int rowsPerPage = Integer.valueOf(req.getParameter("limit"));
			if (written) {
				if (existed) {
					final URI query = storage.selectSavedQuery(http, userName, queryName);
					storage.updateQuery(query, userName, shared, queryLanguage, queryText, infer, rowsPerPage);
				}
				else {
					storage.saveQuery(http, queryName, userName, shared, queryLanguage, queryText, infer,
							rowsPerPage);
				}
			}
			json.put("written", written);
		}
		final PrintWriter writer = new PrintWriter(new BufferedWriter(resp.getWriter()));
		writer.write(json.toString());
		writer.flush();
	}

	private void setContentType(final WorkbenchRequest req, final HttpServletResponse resp) {
		String result = "application/xml";
		String ext = "xml";
		if (req.isParameterPresent(ACCEPT)) {
			final String accept = req.getParameter(ACCEPT);
			final RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				result = format.getDefaultMIMEType();
				ext = format.getDefaultFileExtension();
			}
			else {
				final TupleQueryResultFormat tupleFormat = TupleQueryResultFormat.forMIMEType(accept);

				if (tupleFormat != null) {
					result = tupleFormat.getDefaultMIMEType();
					ext = tupleFormat.getDefaultFileExtension();
				}
				else {
					final BooleanQueryResultFormat booleanFormat = BooleanQueryResultFormat.forMIMEType(accept);

					if (booleanFormat != null) {
						result = booleanFormat.getDefaultMIMEType();
						ext = booleanFormat.getDefaultFileExtension();
					}
				}
			}
		}

		resp.setContentType(result);
		if (!result.equals("application/xml")) {
			final String attachment = "attachment; filename=query." + ext;
			resp.setHeader("Content-disposition", attachment);
		}
	}

	private void service(final WorkbenchRequest req, final HttpServletResponse resp, final OutputStream out,
			final String xslPath)
		throws BadRequestException, OpenRDFException, UnsupportedQueryResultFormatException, IOException
	{
		final RepositoryConnection con = repository.getConnection();
		try {
			final TupleResultBuilder builder = getTupleResultBuilder(req, resp, resp.getOutputStream());
			for (Namespace ns : Iterations.asList(con.getNamespaces())) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			if (req.isParameterPresent(QUERY)) {
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
				builder.link(Arrays.asList(INFO, "namespaces"));
				builder.end();
			}
		}
		finally {
			con.close();
		}
	}
}