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
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.resources.helpers.BooleanResultResource;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.Query;
import org.openrdf.result.BooleanResult;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class BooleanQueryResource extends BooleanResultResource {

	public BooleanQueryResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	@Override
	protected String getFilenamePrefix() {
		return "query-result";
	}

	@Override
	public BooleanResult getBooleanResult()
		throws ResourceException
	{
		Query query = RequestAtt.getQuery(getRequest());

		if (query == null) {
			// query is expected to be available in the request attributes
			throw new ResourceException(SERVER_ERROR_INTERNAL, "missing query attribute");
		}

		if (!(query instanceof BooleanQuery)) {
			throw new ResourceException(SERVER_ERROR_INTERNAL, "unexpected query type: "
					+ query.getClass().getName());
		}

		try {
			return ((BooleanQuery)query).evaluate();
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	public void acceptRepresentation(Representation representation)
		throws ResourceException
	{
		getResponse().setEntity(represent());
	}
}
