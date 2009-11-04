/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ConnectionResource extends SesameResource {

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
		ServerConnection connection = getConnection();

		if (connection != null) {
			try {
				connection.close();
				return null;
			}
			catch (StoreException e) {
				throw new ResourceException(e);
			}
		}

		return null;
	}
}
