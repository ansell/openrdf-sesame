/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.filters.PreparedQueryResolver;
import org.openrdf.http.server.resources.helpers.SesameResource;

/**
 * @author Arjohn Kampman
 */
public class PreparedQueryResource extends SesameResource {

	public PreparedQueryResource(Context context, Request request, Response response) {
		super(context, request, response);
		this.setReadable(false);
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public void removeRepresentations()
		throws ResourceException
	{
		String id = PreparedQueryResolver.getQueryID(getRequest());
		getConnection().removeQuery(id);
	}
}
