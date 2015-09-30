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
package org.eclipse.rdf4j.query.dawg;

import org.eclipse.rdf4j.model.Graph;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.impl.GraphImpl;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.TupleQueryResult;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.impl.TupleQueryResultBuilder;
import org.eclipse.rdf4j.rio.RDFHandlerException;
import org.eclipse.rdf4j.rio.helpers.StatementCollector;

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
