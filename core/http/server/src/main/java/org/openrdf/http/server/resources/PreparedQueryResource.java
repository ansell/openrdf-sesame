/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.filters.PreparedQueryResolver;
import org.openrdf.http.server.resources.helpers.SesameResource;

/**
 * @author Arjohn Kampman
 */
public class PreparedQueryResource extends SesameResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
		setConditional(false);
	}

	@Override
	protected Representation delete()
		throws ResourceException
	{
		String id = PreparedQueryResolver.getQueryID(getRequest());
		getConnection().removeQuery(id);
		return null;
	}
}
