/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;
import static org.restlet.data.Status.SERVER_ERROR_NOT_IMPLEMENTED;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.resources.BooleanQueryResource;
import org.openrdf.http.server.resources.GraphQueryResource;
import org.openrdf.http.server.resources.TupleQueryResource;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;

/**
 * Filter that routes queries to a resource that is able to process that
 * particular type of queries. A non-query handler can optionally be specified
 * for handling requests without an associated query.
 * 
 * @author Arjohn Kampman
 */
public class QueryTypeRouter extends Filter {

	private final Restlet nonQueryHandler;

	private final Restlet tupleQueryHandler;

	private final Restlet graphQueryHandler;

	private final Restlet booleanQueryHandler;

	public QueryTypeRouter(Context context) {
		this(context, (Restlet)null);
	}

	public QueryTypeRouter(Context context, Class<? extends ServerResource> nonQueryHandler) {
		this(context, new Finder(context, nonQueryHandler));
	}

	public QueryTypeRouter(Context context, Restlet nonQueryHandler) {
		super(context);
		this.nonQueryHandler = nonQueryHandler;
		tupleQueryHandler = new Finder(context, TupleQueryResource.class);
		graphQueryHandler = new Finder(context, GraphQueryResource.class);
		booleanQueryHandler = new Finder(context, BooleanQueryResource.class);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		Query query = RequestAtt.getQuery(request);

		if (query == null) {
			if (nonQueryHandler == null) {
				// query is expected to be available in the request attributes
				response.setStatus(SERVER_ERROR_INTERNAL, "missing query attribute");
				return Filter.STOP;
			}

			setNext(nonQueryHandler);
		}
		else if (query instanceof TupleQuery) {
			setNext(tupleQueryHandler);
		}
		else if (query instanceof GraphQuery) {
			setNext(graphQueryHandler);
		}
		else if (query instanceof BooleanQuery) {
			setNext(booleanQueryHandler);
		}
		else {
			response.setStatus(SERVER_ERROR_NOT_IMPLEMENTED, "Unsupported query type: "
					+ query.getClass().getName());
			return Filter.STOP;
		}

		return Filter.CONTINUE;
	}
}
