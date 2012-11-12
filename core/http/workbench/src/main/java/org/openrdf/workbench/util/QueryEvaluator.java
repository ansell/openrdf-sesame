/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Statement;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;

/**
 * @author dale
 */
public final class QueryEvaluator {

	private static final String INFO = "info";
	
	public static final QueryEvaluator INSTANCE = new QueryEvaluator();
	
	private QueryEvaluator(){
		// do nothing
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
	public void evaluateGraphQuery(final TupleResultBuilder builder, final GraphQuery query)
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

	public int countQueryResults(final GraphQuery query)
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

	public int countQueryResults(final TupleQuery query)
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

	public void evaluateGraphQuery(final RDFWriter writer, final GraphQuery query)
		throws QueryEvaluationException, RDFHandlerException
	{
		query.evaluate(writer);
	}

	public void evaluateBooleanQuery(final TupleResultBuilder builder, final BooleanQuery query)
		throws QueryEvaluationException
	{
		final boolean result = query.evaluate();
		builder.link(INFO);
		builder.bool(result);
	}

}
