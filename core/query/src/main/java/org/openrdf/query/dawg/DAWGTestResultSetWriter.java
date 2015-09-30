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
package org.openrdf.query.dawg;

import static org.openrdf.query.dawg.DAWGTestResultSetSchema.BINDING;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTSET;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTVARIABLE;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.SOLUTION;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.VALUE;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.VARIABLE;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;

/**
 * A {@link TupleQueryResultHandler} that converts query results to an RDF graph
 * using the Data Access Working Group Test Result Set RDF Vocabulary
 * (http://www.w3.org/2001/sw/DataAccess/tests/result-set#).
 */
public class DAWGTestResultSetWriter implements TupleQueryResultHandler {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * RDFHandler to report the generated statements to.
	 */
	private final RDFHandler rdfHandler;

	private final ValueFactory vf;

	/*-----------*
	 * Variables *
	 *-----------*/

	private BNode resultSetNode;

	private Map<BNode, BNode> bnodeMap = new HashMap<BNode, BNode>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DAWGTestResultSetWriter(RDFHandler rdfHandler) {
		this(rdfHandler, SimpleValueFactory.getInstance());
	}

	public DAWGTestResultSetWriter(RDFHandler rdfHandler, ValueFactory vf) {
		this.rdfHandler = rdfHandler;
		this.vf = vf;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void startQueryResult(List<String> bindingNames)
		throws TupleQueryResultHandlerException
	{
		try {
			rdfHandler.startRDF();

			resultSetNode = vf.createBNode();
			bnodeMap.clear();

			reportStatement(resultSetNode, RDF.TYPE, RESULTSET);

			for (String bindingName : bindingNames) {
				Literal bindingNameLit = vf.createLiteral(bindingName);
				reportStatement(resultSetNode, RESULTVARIABLE, bindingNameLit);
			}
		}
		catch (RDFHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void endQueryResult()
		throws TupleQueryResultHandlerException
	{
		resultSetNode = null;

		try {
			rdfHandler.endRDF();
		}
		catch (RDFHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	@Override
	public void handleSolution(BindingSet bindingSet)
		throws TupleQueryResultHandlerException
	{
		try {
			BNode solutionNode = vf.createBNode();

			reportStatement(resultSetNode, SOLUTION, solutionNode);

			for (Binding binding : bindingSet) {
				BNode bindingNode = vf.createBNode();

				reportStatement(solutionNode, BINDING, bindingNode);
				reportStatement(bindingNode, VARIABLE, vf.createLiteral(binding.getName()));

				Value value = binding.getValue();

				// Map bnodes to new bnodes to prevent collisions with the bnodes
				// generated for the result format
				if (value instanceof BNode) {
					BNode mappedBNode = bnodeMap.get(value);

					if (mappedBNode == null) {
						mappedBNode = vf.createBNode();
						bnodeMap.put((BNode)value, mappedBNode);
					}

					value = mappedBNode;
				}

				reportStatement(bindingNode, VALUE, value);
			}
		}
		catch (RDFHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

	private void reportStatement(Resource subject, IRI predicate, Value object)
		throws RDFHandlerException
	{
		rdfHandler.handleStatement(vf.createStatement(subject, predicate, object));
	}

	@Override
	public void handleBoolean(boolean value)
		throws QueryResultHandlerException
	{
		throw new UnsupportedOperationException("Cannot handle boolean results");
	}

	@Override
	public void handleLinks(List<String> linkUrls)
		throws QueryResultHandlerException
	{
		// We do not support links, so do nothing
	}
}
