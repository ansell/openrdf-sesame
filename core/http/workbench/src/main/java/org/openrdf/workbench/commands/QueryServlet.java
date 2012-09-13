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
import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.workbench.base.TransformationServlet;
import org.openrdf.workbench.exceptions.BadRequestException;
import org.openrdf.workbench.util.TupleResultBuilder;
import org.openrdf.workbench.util.WorkbenchRequest;

public class QueryServlet extends TransformationServlet {

	private Logger logger = LoggerFactory.getLogger(QueryServlet.class);

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer" };
	}

	@Override
	protected void service(WorkbenchRequest req, HttpServletResponse resp, String xslPath)
		throws Exception, IOException
	{
		if (req.isParameterPresent("Accept")) {
			String accept = req.getParameter("Accept");
			RDFFormat format = RDFFormat.forMIMEType(accept);
			if (format != null) {
				resp.setContentType(accept);
				String ext = format.getDefaultFileExtension();
				String attachment = "attachment; filename=query." + ext;
				resp.setHeader("Content-disposition", attachment);
			}
		}
		else {
			resp.setContentType("application/xml");
		}
		PrintWriter out = resp.getWriter();
		try {
			PrintWriter writer = new PrintWriter(new BufferedWriter(out));
			service(req, writer, xslPath);
			writer.flush();
		}
		catch (BadRequestException exc) {
			logger.warn(exc.toString(), exc);
			resp.setContentType("application/xml");
			TupleResultBuilder builder = new TupleResultBuilder(out);
			builder.transform(xslPath, "query.xsl");
			builder.start("error-message");
			builder.link("info");
			builder.link("namespaces");
			builder.result(exc.getMessage());
			builder.end();
		}
	}

	private void service(WorkbenchRequest req, PrintWriter out, String xslPath)
		throws Exception
	{
		RepositoryConnection con = repository.getConnection();
		try {
			TupleResultBuilder builder = new TupleResultBuilder(out);
			for (Namespace ns : con.getNamespaces().asList()) {
				builder.prefix(ns.getPrefix(), ns.getName());
			}
			if (req.isParameterPresent("query")) {
				try {
					service(builder, out, xslPath, con, req);
				}
				catch (MalformedQueryException exc) {
					throw new BadRequestException(exc.getMessage(), exc);
				}
				catch (HTTPQueryEvaluationException exc) {
					if (exc.getCause() instanceof MalformedQueryException) {
						throw new BadRequestException(exc.getCause().getMessage());
					}
					throw exc;
				}
			}
			else {
				builder.transform(xslPath, "query.xsl");
				builder.start();
				builder.link("info");
				builder.link("namespaces");
				builder.end();
			}
		}
		finally {
			con.close();
		}
	}

	private void service(TupleResultBuilder builder, PrintWriter out, String xslPath,
			RepositoryConnection con, WorkbenchRequest req)
		throws Exception
	{
		String ql = req.getParameter("queryLn");
		String q = req.getParameter("query");
		Query query = prepareQuery(con, QueryLanguage.valueOf(ql), q);
		if (req.isParameterPresent("infer")) {
			boolean infer = Boolean.parseBoolean(req.getParameter("infer"));
			query.setIncludeInferred(infer);
		}
		int limit = req.getInt("limit");
		int offset = req.getInt("offset");
		RDFFormat format = null;
		if (req.isParameterPresent("Accept")) {
			format = RDFFormat.forMIMEType(req.getParameter("Accept"));
		}
		if (query instanceof TupleQuery) {
			builder.transform(xslPath, "tuple.xsl");
			builder.start();
			evaluateTupleQuery(builder, (TupleQuery)query, limit, offset);
			builder.end();
		}
		else if (query instanceof GraphQuery && format == null) {
			builder.transform(xslPath, "graph.xsl");
			builder.start();
			evaluateGraphQuery(builder, (GraphQuery)query, limit, offset);
			builder.end();
		}
		else if (query instanceof GraphQuery) {
			RDFWriterFactory factory = getInstance().get(format);
			RDFWriter writer = factory.getWriter(out);
			evaluateGraphQuery(writer, (GraphQuery)query);
		}
		else if (query instanceof BooleanQuery) {
			builder.transform(xslPath, "boolean.xsl");
			builder.start();
			evaluateBooleanQuery(builder, (BooleanQuery)query);
			builder.end();
		}
		else {
			throw new BadRequestException("Unknown query type: " + query.getClass().getSimpleName());
		}
	}
	
	private Query prepareQuery(RepositoryConnection con, QueryLanguage ql, String q)
		throws RepositoryException, MalformedQueryException
	{
		try {
			return con.prepareQuery(ql, q);
		}
		catch (UnsupportedOperationException exc) {
			// TODO must be an http repository
			try {
				con.prepareTupleQuery(ql, q).evaluate().close();
				return con.prepareTupleQuery(ql, q);
			}
			catch (Exception malformed) {
				// guess its not a tuple query
			}
			try {
				con.prepareGraphQuery(ql, q).evaluate().close();
				return con.prepareGraphQuery(ql, q);
			}
			catch (Exception malformed) {
				// guess its not a graph query
			}
			try {
				con.prepareBooleanQuery(ql, q).evaluate();
				return con.prepareBooleanQuery(ql, q);
			}
			catch (Exception malformed) {
				// guess its not a boolean query
			}
			// let's assume it is an malformed tuple query
			return con.prepareTupleQuery(ql, q);
		}
	}

	/***
	 * Evaluate a tuple query, returning a subset of the results defined by 
	 * limit and offset.
	 * 
	 * @param builder response builder helper for generating the XML response
	 * to the client
	 * @param query the query to be evaluated
	 * @param limit the maximum number of results to return in the response
	 * document
	 * @param offset result count at which to start inserting into response
	 * document
	 */
	private void evaluateTupleQuery(TupleResultBuilder builder, 
			TupleQuery query, int limit, int offset)
		throws QueryEvaluationException
	{
		TupleQueryResult result = query.evaluate();
		try {
			String[] names = result.getBindingNames().toArray(new String[0]);
			builder.variables(names);
			builder.link("info");
			builder.flush();
			final int max = offset + limit;
			if (max > 0) {
				for (int l = 0; l < max && result.hasNext(); l++) {
					BindingSet set = result.next();
					if (l < offset) {
						continue;
					}
					Object[] values = new Object[names.length];
					for (int i = 0; i < names.length; i++) {
						values[i] = set.getValue(names[i]);
					}
					builder.result(values);
				}
			}
		}
		finally {
			result.close();
		}
	}

	/***
	 * Evaluate a graph query, returning a subset of the results defined by 
	 * limit and offset.
	 * 
	 * @param builder response builder helper for generating the XML response
	 * to the client
	 * @param query the query to be evaluated
	 * @param limit the maximum number of results to return in the response
	 * document
	 * @param offset result count at which to start inserting  into response
	 * document
	 */
	private void evaluateGraphQuery(TupleResultBuilder builder, 
			GraphQuery query, int limit, int offset)
		throws QueryEvaluationException
	{
		GraphQueryResult result = query.evaluate();
		try {
			builder.variables("subject", "predicate", "object");
			builder.link("info");
			final int max = offset + limit;
			if (max >= 0) {
			    for (int l = 0; l < max && result.hasNext(); l++) {
				    Statement st = result.next();
				    if (l < offset) {
					    continue;
				    }
				    builder.result(st.getSubject(), st.getPredicate(), st.getObject(), st.getContext());
			    }
			}
		}
		finally {
			result.close();
		}
	}

	private void evaluateGraphQuery(RDFWriter writer, GraphQuery query)
		throws QueryEvaluationException, RDFHandlerException
	{
		query.evaluate(writer);
	}

	private void evaluateBooleanQuery(TupleResultBuilder builder, BooleanQuery query)
		throws QueryEvaluationException
	{
		boolean result = query.evaluate();
		builder.link("info");
		builder.bool(result);
	}

}