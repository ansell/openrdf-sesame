/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import info.aduna.iteration.Iterations;

import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.util.ModelUtil;
import org.openrdf.query.dawg.DAWGTestResultSetUtil;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * Class offering utility methods related to query results.
 * 
 * @author Arjohn Kampman
 */
public class QueryResultUtil {

	/**
	 * Reports a tuple query result to a {@link TupleQueryResultHandler}.
	 * 
	 * @param tqr
	 *        The query result to report.
	 * @param handler
	 *        The handler to report the query result to.
	 * @throws TupleQueryResultHandlerException
	 *         If such an exception is thrown by the used query result writer.
	 */
	public static void report(TupleQueryResult tqr, TupleQueryResultHandler handler)
		throws TupleQueryResultHandlerException, QueryEvaluationException
	{
		handler.startQueryResult(tqr.getBindingNames(), tqr.isDistinct(), tqr.isOrdered());
		try {
			while (tqr.hasNext()) {
				BindingSet bindingSet = tqr.next();
				handler.handleSolution(bindingSet);
			}
		}
		finally {
			tqr.close();
		}
		handler.endQueryResult();
	}

	/**
	 * Reports a graph query result to an {@link RDFHandler}.
	 * 
	 * @param gqr
	 *        The query result to report.
	 * @param rdfHandler
	 *        The handler to report the query result to.
	 * @throws RDFHandlerException
	 *         If such an exception is thrown by the used RDF writer.
	 * @throws QueryEvaluationException
	 */
	public static void report(GraphQueryResult gqr, RDFHandler rdfHandler)
		throws RDFHandlerException, QueryEvaluationException
	{
		try {
			rdfHandler.startRDF();

			for (Map.Entry<String, String> entry : gqr.getNamespaces().entrySet()) {
				String prefix = entry.getKey();
				String namespace = entry.getValue();
				rdfHandler.handleNamespace(prefix, namespace);
			}

			while (gqr.hasNext()) {
				Statement st = gqr.next();
				rdfHandler.handleStatement(st);
			}

			rdfHandler.endRDF();
		}
		finally {
			gqr.close();
		}
	}

	/**
	 * Compares the two query results by converting them to graphs and returns
	 * true if they are equal. QueryResults are equal if they contain the same
	 * set of BindingSet and have the headers. Blank nodes identifiers are not
	 * relevant for equality, they are mapped from one model to the other by
	 * using the attached properties. Note that the method consumes both query
	 * results fully.
	 * 
	 * @throws QueryEvaluationException
	 */
	public static boolean equals(TupleQueryResult tqr1, TupleQueryResult tqr2)
		throws QueryEvaluationException
	{
		Collection<? extends Statement> graph1 = DAWGTestResultSetUtil.toGraph(tqr1);
		Collection<? extends Statement> graph2 = DAWGTestResultSetUtil.toGraph(tqr2);

		return ModelUtil.equals(graph1, graph2);
	}

	public static boolean equals(GraphQueryResult result1, GraphQueryResult result2)
		throws QueryEvaluationException
	{
		Set<? extends Statement> graph1 = Iterations.asSet(result1);
		Set<? extends Statement> graph2 = Iterations.asSet(result1);

		return ModelUtil.equals(graph1, graph2);
	}

	/**
	 * Check whether two {@link BindingSet}s are compatible. Two binding sets
	 * are compatible if they have equal values for each binding name that occurs
	 * in both binding sets.
	 */
	public static boolean bindingSetsCompatible(BindingSet bs1, BindingSet bs2) {
		Set<String> sharedBindings = new HashSet<String>(bs1.getBindingNames());
		sharedBindings.retainAll(bs2.getBindingNames());

		for (String bindingName : sharedBindings) {
			Value value1 = bs1.getValue(bindingName);
			Value value2 = bs2.getValue(bindingName);

			if (!value1.equals(value2)) {
				return false;
			}
		}

		return true;
	}
}
