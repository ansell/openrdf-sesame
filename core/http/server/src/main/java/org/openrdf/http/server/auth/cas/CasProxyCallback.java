/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.auth.cas;

import org.restlet.data.Status;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * @author Arjohn Kampman
 */
public class CasProxyCallback extends ServerResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
	}

	@Override
	protected Representation get()
		throws ResourceException
	{
		String pgtId = getQuery().getFirstValue("pgtId");
		String pgtIou = getQuery().getFirstValue("pgtIou");

		if (pgtId != null && pgtIou != null) {
			return new EmptyRepresentation();
		}
		else {
			getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
			return null;
		}
	}
}
