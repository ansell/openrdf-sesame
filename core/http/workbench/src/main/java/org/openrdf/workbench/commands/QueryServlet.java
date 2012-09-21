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
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

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
	
    private static final int flags = Pattern.CASE_INSENSITIVE | 
	    		Pattern.MULTILINE | Pattern.DOTALL;
	private static final Pattern limitOrOffset = 
			Pattern.compile("((limit)|(offset))\\s+\\d+", flags);
	private static final Pattern offset_pattern = 
			Pattern.compile("\\boffset\\s+\\d+\\b", flags);
	private static final Pattern limit_pattern = 
			Pattern.compile("\\blimit\\s+\\d+\\b", flags);
	private static final Pattern splitter = Pattern.compile("\\s");
	private static final Pattern serql_namespace = 
			Pattern.compile("\\busing namespace\\b", flags);

	@Override
	public String[] getCookieNames() {
		return new String[] { "limit", "queryLn", "infer", "total_result_count"};
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
			service(req, resp, writer, xslPath);
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

	private void service(WorkbenchRequest req, HttpServletResponse resp, PrintWriter out, String xslPath)
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
					service(builder, resp, out, xslPath, con, req);
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

	private void addTotalResultCountCookie(WorkbenchRequest req, HttpServletResponse resp, int value) {
		Cookie cookie = new Cookie("total_result_count", String.valueOf(value));
		if (req.getContextPath() != null) {
			cookie.setPath(req.getContextPath());
		}
		else {
			cookie.setPath("/");
		}
		cookie.setMaxAge(Integer.parseInt(config.getInitParameter(COOKIE_AGE_PARAM)));
		addCookie(req, resp, cookie);
	}
	
	private void service(TupleResultBuilder builder, HttpServletResponse resp, PrintWriter out, 
			String xslPath, RepositoryConnection con, WorkbenchRequest req)
		throws Exception
	{
		final QueryLanguage ql = QueryLanguage.valueOf(
				req.getParameter("queryLn"));
		final int limit = req.getInt("limit");
		final int offset = req.getInt("offset");
		String q = req.getParameter("query");
		Query query = prepareQuery(con, ql, q);
		if (query instanceof GraphQuery || query instanceof TupleQuery) {
			final boolean know_total = req.getBoolean("know_total", false);
			if (!know_total) {
				final int total_result_count = 
					(query instanceof GraphQuery) ? 
					countQueryResults(builder, (GraphQuery)query) :
					countQueryResults(builder, (TupleQuery)query);
				addTotalResultCountCookie(req, resp, total_result_count);
			}
			q = modifyQuery(q, ql, limit, offset);
			query = prepareQuery(con, ql, q);
		}
		if (req.isParameterPresent("infer")) {
			boolean infer = Boolean.parseBoolean(req.getParameter("infer"));
			query.setIncludeInferred(infer);
		}
		RDFFormat format = null;
		if (req.isParameterPresent("Accept")) {
			format = RDFFormat.forMIMEType(req.getParameter("Accept"));
		}
		if (query instanceof TupleQuery) {
			builder.transform(xslPath, "tuple.xsl");
			builder.start();
			evaluateTupleQuery(builder, (TupleQuery)query);
			builder.end();
		}
		else if (query instanceof GraphQuery && format == null) {
			builder.transform(xslPath, "graph.xsl");
			builder.start();
			evaluateGraphQuery(builder, (GraphQuery)query);
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
	
	/***
	 * Add or modify the limit and offset clauses of the query to be executed
	 * so that only those results to be displayed are requested from the
	 * query engine.
	 * 
	 * @param query as it was specified by the user
	 * @param language SPARQL or SeRQL, as specified by the user
	 * @param requestLimit maximum number of results to return, as specified 
	 * by the URL query parameters or cookies
	 * @param requestOffset which result to start at when populating the 
	 * result set
	 * @returns the user's query with appended or modified LIMIT and OFFSET 
	 * clauses
	 */
	private String modifyQuery(final String query, 
			final QueryLanguage language, 
			final int requestLimit, int requestOffset) {
		// gracefully handle malicious value
		if (requestOffset < 0) requestOffset = 0; 
		logger.info("Query Language: {}, requestLimit: " + requestLimit + 
				", requestOffset: " + requestOffset, language);
		logger.info("Query: {}", query);
		String rval = query;
		
		// requestLimit <= 0 actually means don't limit display
		if (requestLimit > 0) { 
			/* the matcher on the pattern will have a group for "limit l#" as 
		       well as a group for l#, similarly for "offset o#" and o#. If 
		       either doesn't exist, it can be appended at the end. */  
			int queryLimit = -1;
			int queryOffset = -1;
			final Matcher m = limitOrOffset.matcher(query);
			while(m.find()){
				final String clause = m.group();
				final int value = Integer.parseInt(
						splitter.split(clause)[1]);
				if (clause.startsWith("limit")){
					queryLimit = value;
				} else {
					queryOffset =  value;
				}
			}
			
			final boolean queryLimitExists = (queryLimit >= 0);
			final boolean queryOffsetExists = (queryOffset >= 0);
			final int maxQueryResultCount = queryLimitExists ? 
					(queryLimit + (queryOffsetExists ? queryOffset : 0)) : 
						Integer.MAX_VALUE;
			final int maxRequestResultCount = requestLimit + requestOffset;
			final int limitSubstitute = 
					(maxRequestResultCount < maxQueryResultCount) ?
					requestLimit : queryLimit - requestOffset;
			/* In SPARQL, LIMIT and/or OFFSET can occur at the end, in 
			 * either order. In SeRQL, LIMIT and/or OFFSET must be 
			 * immediately prior to the *optional* namespace declaration 
			 * section (which is itself last), and LIMIT must precede OFFSET.
			 * This code makes no attempt to correct if the user places them
			 * out of order in the query. 
			 */
			if (queryLimitExists) {
				if (limitSubstitute != queryLimit) {
					// do a clause replacement
					final Matcher lm = limit_pattern.matcher(rval);
					final StringBuffer sb = new StringBuffer();
					lm.find();
					lm.appendReplacement(sb, "limit " + limitSubstitute);
					lm.appendTail(sb);
					rval = sb.toString();
				}
			} else { 
				final String newLimitClause = "limit " + limitSubstitute;
				if (QueryLanguage.SPARQL == language) {
					// add the clause at the end
					if (!rval.endsWith("\n")) {
						rval = rval + '\n';
					}

					rval = rval + newLimitClause;
				} else {
					/* SeRQL, add the clause before any offset clause or the 
					 * namespace section
					 */
					final Pattern p = queryOffsetExists ? offset_pattern :
						serql_namespace;
					rval = insertAtMatchOnOwnLine(p, rval, newLimitClause);
				}
			}

			if (queryOffsetExists) {
				final int offsetSubstitute = queryOffset + requestOffset;
				if (offsetSubstitute != requestOffset) {
					// do a clause replacement
					final Matcher om = offset_pattern.matcher(rval);
					final StringBuffer sb = new StringBuffer();
					om.find();
					om.appendReplacement(sb, "offset " + offsetSubstitute);
					om.appendTail(sb);
					rval = sb.toString();
				}
			} else {
				final String newOffsetClause = "offset " + requestOffset;
				if (QueryLanguage.SPARQL == language) {
					if (requestOffset > 0) {
						// add offset clause
						if (!rval.endsWith("\n")) {
							rval = rval + '\n';
						}

					rval = rval + newOffsetClause;
					}
				} else {
					/* SeRQL, add the clause before before the namespace
					 * section
					 */
					rval = insertAtMatchOnOwnLine(serql_namespace, rval, newOffsetClause);
				}
			}
			
			logger.info("Modified Query: {}", rval);
		}
		
		return rval;
	}
		
    /**
     * Insert a given string into another string at the point at which the 
     * given matcher matches, making sure to place the insertion string on 
     * its own line. If there is no match, appends to end on own line.
     * 
     * @param p pattern to search for insertion location
     * @param orig string to perform insertion on
     * @param insert string to insert on own line
     * @returns result of inserting text
     */
    private String insertAtMatchOnOwnLine(final Pattern p, 
    		final String orig, final String insert){
    	final Matcher qm = p.matcher(orig);
    	final boolean found = qm.find();
    	final int location = found ? qm.start() : orig.length();
		final StringBuilder builder = new StringBuilder(
				orig.length() + insert.length() + 2);
		builder.append(orig.substring(0, location));
		if (builder.charAt(builder.length()-1) != '\n'){
			builder.append('\n');
		}
		
		builder.append(insert);
		final String end = orig.substring(location);
		if (!end.startsWith("\n")) {
			builder.append('\n');
		}
		
		builder.append(end);
		return builder.toString();
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
	 * Evaluate a tuple query, and create an XML results document.
	 * 
	 * @param builder response builder helper for generating the XML response
	 * to the client
	 * @param query the query to be evaluated
	 */
	private void evaluateTupleQuery(TupleResultBuilder builder, 
			TupleQuery query)
		throws QueryEvaluationException
	{
		TupleQueryResult result = query.evaluate();
		try {
			String[] names = result.getBindingNames().toArray(new String[0]);
			builder.variables(names);
			builder.link("info");
     		while (result.hasNext()) {
			    final BindingSet set = result.next();
				Object[] values = new Object[names.length];
				for (int i = 0; i < names.length; i++) {
					values[i] = set.getValue(names[i]);
				}
				builder.result(values);
			}
		}
		finally {
			result.close();
		}
	}

	/***
	 * Evaluate a graph query, and create an XML results document.
	 * 
	 * @param builder response builder helper for generating the XML response
	 * to the client
	 * @param query the query to be evaluated
	 */
	private void evaluateGraphQuery(TupleResultBuilder builder, 
			GraphQuery query)
		throws QueryEvaluationException
	{
		GraphQueryResult result = query.evaluate();
		try {
			builder.variables("subject", "predicate", "object");
			builder.link("info");
		    while (result.hasNext()) {
				final Statement st = result.next();
			    builder.result(st.getSubject(), st.getPredicate(), 
			    		st.getObject(), st.getContext());
			}
		}
		finally {
			result.close();
		}
	}
	
	private int countQueryResults(TupleResultBuilder builder, GraphQuery query)
		throws QueryEvaluationException
	{
		int rval = 0;
		GraphQueryResult result = query.evaluate();
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
	
	private int countQueryResults(TupleResultBuilder builder, TupleQuery query)
			throws QueryEvaluationException
	{
		int rval = 0;
		TupleQueryResult result = query.evaluate();
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