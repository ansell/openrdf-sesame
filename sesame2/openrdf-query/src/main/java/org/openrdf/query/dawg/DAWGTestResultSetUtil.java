/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.dawg;

import org.openrdf.model.Graph;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.GraphImpl;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.StatementCollector;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetUtil {

	public static TupleQueryResult toTupleQueryResult(Iterable<? extends Statement> dawgGraph)
		throws DAWGTestResultSetParseException
	{
		TupleQueryResultBuilder tqrBuilder = new TupleQueryResultBuilder();
		DAWGTestResultSetParser parser = new DAWGTestResultSetParser(tqrBuilder);

		try {
			parser.startRDF();
			for (Statement st : dawgGraph) {
				parser.handleStatement(st);
			}
			parser.endRDF();

			return tqrBuilder.getQueryResult();
		}
		catch (RDFHandlerException e) {
			throw new DAWGTestResultSetParseException(e.getMessage(), e);
		}
	}

	public static Graph toGraph(TupleQueryResult tqr)
		throws QueryEvaluationException
	{
		Graph graph = new GraphImpl();
		DAWGTestResultSetWriter writer = new DAWGTestResultSetWriter(new StatementCollector(graph));

		try {
			writer.startQueryResult(tqr.getBindingNames());
			while (tqr.hasNext()) {
				writer.handleSolution(tqr.next());
			}
			writer.endQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// No exceptions expected from DAWGTestResultSetWriter or
			// StatementCollector, foud a bug?
			throw new RuntimeException(e);
		}

		return graph;
	}

	public static boolean toBooleanQueryResult(Iterable<? extends Statement> dawgGraph)
		throws DAWGTestResultSetParseException
	{
		DAWGTestBooleanParser parser = new DAWGTestBooleanParser();

		try {
			parser.startRDF();
			for (Statement st : dawgGraph) {
				parser.handleStatement(st);
			}
			parser.endRDF();

			return parser.getValue();
		}
		catch (RDFHandlerException e) {
			throw new DAWGTestResultSetParseException(e.getMessage(), e);
		}
	}
}
