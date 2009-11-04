/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.query.Query;

/**
 * @author Arjohn Kampman
 */
public class QueryListResource extends SesameResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
		setConditional(false);
	}

	@Override
	protected Representation post(Representation entity)
		throws ResourceException
	{
		Query query = RequestAtt.getQuery(getRequest());

		if (query == null) {
			// query is expected to be available in the request attributes
			throw new ResourceException(SERVER_ERROR_INTERNAL, "missing query attribute");
		}

		String queryID = getConnection().storeQuery(query);

		Reference queryRef = getRequest().getResourceRef().clone();
		queryRef.addSegment(queryID);
		getResponse().setLocationRef(queryRef);

		getResponse().setStatus(Status.SUCCESS_CREATED);
		return null;
	}
}
