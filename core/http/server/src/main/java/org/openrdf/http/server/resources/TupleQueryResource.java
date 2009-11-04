/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.resources.helpers.TupleResultResource;
import org.openrdf.query.Query;
import org.openrdf.query.TupleQuery;
import org.openrdf.result.TupleResult;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class TupleQueryResource extends TupleResultResource {

	@Override
	protected String getFilenamePrefix() {
		return "query-result";
	}

	public TupleResult getTupleResult()
		throws ResourceException
	{
		Query query = RequestAtt.getQuery(getRequest());

		if (query == null) {
			// query is expected to be available in the request attributes
			throw new ResourceException(SERVER_ERROR_INTERNAL, "missing query attribute");
		}

		if (!(query instanceof TupleQuery)) {
			throw new ResourceException(SERVER_ERROR_INTERNAL, "unexpected query type: "
					+ query.getClass().getName());
		}

		try {
			return ((TupleQuery)query).evaluate();
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected Representation post(Representation representation, Variant variant)
		throws ResourceException
	{
		return get(variant);
	}
}
