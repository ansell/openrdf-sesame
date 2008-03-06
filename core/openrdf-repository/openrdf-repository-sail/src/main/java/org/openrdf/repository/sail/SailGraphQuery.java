/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.impl.AbstractQuery;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.query.parser.ParsedGraphQuery;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;

/**
 * @author Arjohn Kampman
 */
class SailGraphQuery extends AbstractQuery implements GraphQuery {

	private ParsedGraphQuery _graphQuery;

	private SailRepositoryConnection _repCon;

	public SailGraphQuery(ParsedGraphQuery tupleQuery, SailRepositoryConnection con) {
		_graphQuery = tupleQuery;
		_repCon = con;
	}

	public GraphQueryResult evaluate()
		throws QueryEvaluationException
	{
		TupleExpr tupleExpr = _graphQuery.getTupleExpr();

		try {
			SailConnection sailCon = _repCon.getSailConnection();
			CloseableIteration<? extends BindingSet, QueryEvaluationException> bindingsIter = sailCon.evaluate(
					tupleExpr, _bindings, _includeInferred);

			// Filters out all partial and invalid matches
			bindingsIter = new FilterIteration<BindingSet, QueryEvaluationException>(bindingsIter) {

				protected boolean accept(BindingSet bindingSet) {
					Value context = bindingSet.getValue("context");

					return bindingSet.getValue("subject") instanceof Resource
							&& bindingSet.getValue("predicate") instanceof URI
							&& bindingSet.getValue("object") instanceof Value
							&& (context == null || context instanceof Resource);
				}
			};

			// Convert the BindingSet objects to actual RDF statements
			CloseableIteration<Statement, QueryEvaluationException> stIter;
			stIter = new ConvertingIteration<BindingSet, Statement, QueryEvaluationException>(bindingsIter) {

				protected Statement convert(BindingSet bindingSet) {
					Resource subject = (Resource)bindingSet.getValue("subject");
					URI predicate = (URI)bindingSet.getValue("predicate");
					Value object = bindingSet.getValue("object");
					Resource context = (Resource)bindingSet.getValue("context");

					if (context == null) {
						return _repCon.getRepository().getValueFactory().createStatement(subject, predicate, object);
					}
					else {
						return _repCon.getRepository().getValueFactory().createStatement(subject, predicate,
								object, context);
					}
				}
			};

			return new GraphQueryResultImpl(_graphQuery.getQueryNamespaces(), stIter);
		}
		catch (SailException e) {
			throw new QueryEvaluationException(e);
		}
	}

	public void evaluate(RDFHandler handler)
		throws QueryEvaluationException, RDFHandlerException
	{
		GraphQueryResult queryResult = evaluate();
		QueryResultUtil.report(queryResult, handler);
	}
}
