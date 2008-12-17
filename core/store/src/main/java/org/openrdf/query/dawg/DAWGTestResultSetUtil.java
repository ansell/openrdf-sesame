/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.dawg;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.ModelImpl;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.result.TupleResult;
import org.openrdf.result.util.TupleQueryResultBuilder;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetUtil {

	public static TupleResult toTupleQueryResult(Iterable<? extends Statement> dawgGraph)
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

	public static Model toGraph(TupleResult tqr)
		throws StoreException
	{
		Model model = new ModelImpl();
		DAWGTestResultSetWriter writer = new DAWGTestResultSetWriter(new StatementCollector(model));

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

		return model;
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
