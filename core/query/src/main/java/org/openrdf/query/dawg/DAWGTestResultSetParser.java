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

import static org.openrdf.query.dawg.DAWGTestResultSetSchema.BINDING;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTSET;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.RESULTVARIABLE;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.SOLUTION;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.VALUE;
import static org.openrdf.query.dawg.DAWGTestResultSetSchema.VARIABLE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.Model;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.model.util.Models;
import org.openrdf.model.util.GraphUtilException;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.Binding;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.BindingImpl;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFHandlerBase;

/**
 * @author Arjohn Kampman
 */
public class DAWGTestResultSetParser extends RDFHandlerBase {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * RDFHandler to report the generated statements to.
	 */
	private final TupleQueryResultHandler tqrHandler;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Model graph = new LinkedHashModel();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DAWGTestResultSetParser(TupleQueryResultHandler tqrHandler) {
		this.tqrHandler = tqrHandler;
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void startRDF()
		throws RDFHandlerException
	{
		graph.clear();
	}

	@Override
	public void handleStatement(Statement st)
		throws RDFHandlerException
	{
		graph.add(st);
	}

	@Override
	public void endRDF()
		throws RDFHandlerException
	{
		try {
			Resource resultSetNode = Models.getUniqueSubject(graph, RDF.TYPE, RESULTSET);

			List<String> bindingNames = getBindingNames(resultSetNode);
			tqrHandler.startQueryResult(bindingNames);

			for(Value solutionNode : graph.filter(resultSetNode, SOLUTION, null).subjects()) {
				if (solutionNode instanceof Resource) {
					reportSolution((Resource)solutionNode, bindingNames);
				}
				else {
					throw new RDFHandlerException("Value for " + SOLUTION + " is not a resource: " + solutionNode);
				}
			}

			tqrHandler.endQueryResult();
		}
		catch (GraphUtilException e) {
			throw new RDFHandlerException(e.getMessage(), e);
		}
		catch (TupleQueryResultHandlerException e) {
			throw new RDFHandlerException(e.getMessage(), e);
		}
	}

	private List<String> getBindingNames(Resource resultSetNode)
		throws RDFHandlerException
	{
		List<String> bindingNames = new ArrayList<String>(16);

		for (Value varName : graph.filter(resultSetNode, RESULTVARIABLE, null).subjects()) {
			if (varName instanceof Literal) {
				bindingNames.add(((Literal)varName).getLabel());
			}
			else {
				throw new RDFHandlerException("Value for " + RESULTVARIABLE + " is not a literal: " + varName);
			}
		}

		return bindingNames;
	}

	private void reportSolution(Resource solutionNode, List<String> bindingNames)
		throws RDFHandlerException, GraphUtilException
	{
		MapBindingSet bindingSet = new MapBindingSet(bindingNames.size());

		for(Value bindingNode : graph.filter(solutionNode, BINDING, null).objects()) {
			if (bindingNode instanceof Resource) {
				Binding binding = getBinding((Resource)bindingNode);
				bindingSet.addBinding(binding);
			}
			else {
				throw new RDFHandlerException("Value for " + BINDING + " is not a resource: " + bindingNode);
			}
		}

		try {
			tqrHandler.handleSolution(bindingSet);
		}
		catch (TupleQueryResultHandlerException e) {
			throw new RDFHandlerException(e.getMessage(), e);
		}
	}

	private Binding getBinding(Resource bindingNode)
		throws GraphUtilException
	{
		Literal name = Models.getUniqueObjectLiteral(graph, bindingNode, VARIABLE);
		Value value = Models.getUniqueObject(graph, bindingNode, VALUE);
		return new BindingImpl(name.getLabel(), value);
	}
}
