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
package org.openrdf.repository.sail;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResults;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.IteratingGraphQueryResult;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Arjohn Kampman
 */
public class SailGraphQuery extends SailQuery implements GraphQuery {

	protected SailGraphQuery(ParsedGraphQuery tupleQuery, SailRepositoryConnection con) {
		super(tupleQuery, con);
	}

	@Override
	public ParsedGraphQuery getParsedQuery() {
		return (ParsedGraphQuery)super.getParsedQuery();
	}

	public GraphQueryResult evaluate()
		throws QueryEvaluationException
	{
		TupleExpr tupleExpr = getParsedQuery().getTupleExpr();

		try {
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter;
			
			SailConnection sailCon = getConnection().getSailConnection();
			bindingsIter = sailCon.evaluate(tupleExpr, getActiveDataset(), getBindings(), getIncludeInferred());

			// Filters out all partial and invalid matches
			bindingsIter = new FilterIteration<BindingSet, QueryEvaluationException>(bindingsIter) {

				@Override
				protected boolean accept(BindingSet bindingSet) {
					Value context = bindingSet.getValue("context");

					return bindingSet.getValue("subject") instanceof Resource
							&& bindingSet.getValue("predicate") instanceof IRI
							&& bindingSet.getValue("object") instanceof Value
							&& (context == null || context instanceof Resource);
				}
			};
			
			bindingsIter = enforceMaxQueryTime(bindingsIter);

			// Convert the BindingSet objects to actual RDF statements
			final ValueFactory vf = getConnection().getRepository().getValueFactory();
			CloseableIteration<Statement, QueryEvaluationException> stIter;
			stIter = new ConvertingIteration<BindingSet, Statement, QueryEvaluationException>(bindingsIter) {

				@Override
				protected Statement convert(BindingSet bindingSet) {
					Resource subject = (Resource)bindingSet.getValue("subject");
					IRI predicate = (IRI)bindingSet.getValue("predicate");
					Value object = bindingSet.getValue("object");
					Resource context = (Resource)bindingSet.getValue("context");

					if (context == null) {
						return vf.createStatement(subject, predicate, object);
					}
					else {
						return vf.createStatement(subject, predicate, object, context);
					}
				}
			};

			return new IteratingGraphQueryResult(getParsedQuery().getQueryNamespaces(), stIter);
		}
		catch (SailException e) {
			throw new QueryEvaluationException(e.getMessage(), e);
		}
	}

	public void evaluate(RDFHandler handler)
		throws QueryEvaluationException, RDFHandlerException
	{
		GraphQueryResult queryResult = evaluate();
		QueryResults.report(queryResult, handler);
	}
}
