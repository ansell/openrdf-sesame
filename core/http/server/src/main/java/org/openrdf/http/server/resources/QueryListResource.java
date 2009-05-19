/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.query.Query;

/**
 * @author Arjohn Kampman
 */
public class QueryListResource extends SesameResource {

	public QueryListResource(Context context, Request request, Response response) {
		super(context, request, response);
		this.setReadable(false);
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void acceptRepresentation(Representation entity)
		throws ResourceException
	{
		Query query = RequestAtt.getQuery(getRequest());

		if (query == null) {
			// query is expected to be available in the request attributes
			throw new ResourceException(SERVER_ERROR_INTERNAL, "missing query attribute");
		}

		String queryID = getConnection().storeQuery(query);

		getResponse().setLocationRef(queryID);
		getResponse().setStatus(Status.SUCCESS_CREATED);
	}
}
