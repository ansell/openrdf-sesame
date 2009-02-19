/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.sail;

import org.openrdf.cursor.ConvertingCursor;
import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.FilteringCursor;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.parser.GraphQueryModel;
import org.openrdf.result.GraphResult;
import org.openrdf.result.impl.GraphResultImpl;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SailGraphQuery extends SailQuery implements GraphQuery {

	protected SailGraphQuery(GraphQueryModel tupleQuery, SailRepositoryConnection con) {
		super(tupleQuery, con);
	}

	@Override
	public GraphQueryModel getParsedQuery() {
		return (GraphQueryModel)super.getParsedQuery();
	}

	public GraphResult evaluate()
		throws StoreException
	{
		GraphQueryModel query = getParsedQuery();


		Cursor<? extends BindingSet> bindingsIter = evaluate(query);

		// Filters out all partial and invalid matches
		bindingsIter = new FilteringCursor<BindingSet>(bindingsIter) {

			@Override
			protected boolean accept(BindingSet bindingSet) {
				Value context = bindingSet.getValue("context");

				return bindingSet.getValue("subject") instanceof Resource
				&& bindingSet.getValue("predicate") instanceof URI
				&& bindingSet.getValue("object") instanceof Value
				&& (context == null || context instanceof Resource);
			}

			@Override
			public String getName() {
				return "FilterOutPartialMatches";
			}
		};

		// Convert the BindingSet objects to actual RDF statements
		final ValueFactory vf = getConnection().getValueFactory();
		Cursor<Statement> stIter;
		stIter = new ConvertingCursor<BindingSet, Statement>(bindingsIter) {

			@Override
			protected Statement convert(BindingSet bindingSet) {
				Resource subject = (Resource)bindingSet.getValue("subject");
				URI predicate = (URI)bindingSet.getValue("predicate");
				Value object = bindingSet.getValue("object");
				Resource context = (Resource)bindingSet.getValue("context");

				if (context == null) {
					return vf.createStatement(subject, predicate, object);
				}
				else {
					return vf.createStatement(subject, predicate, object, context);
				}
			}

			@Override
			protected String getName() {
				return "CreateStatement";
			}
		};

		return new GraphResultImpl(query.getQueryNamespaces(), stIter);
	}

	public <H extends RDFHandler> H evaluate(H handler)
		throws StoreException, RDFHandlerException
	{
		GraphResult queryResult = evaluate();
		QueryResultUtil.report(queryResult, handler);
		return handler;
	}
}
