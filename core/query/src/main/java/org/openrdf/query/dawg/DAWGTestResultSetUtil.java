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
package org.openrdf.query.dawg;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.model.impl.LinkedHashModel;
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

	public static Model toGraph(TupleQueryResult tqr)
		throws QueryEvaluationException
	{
		Model graph = new LinkedHashModel();
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
