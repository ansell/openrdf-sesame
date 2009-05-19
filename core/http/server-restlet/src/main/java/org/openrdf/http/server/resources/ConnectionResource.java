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

import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ConnectionResource extends SesameResource {

	public ConnectionResource(Context context, Request request, Response response) {
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
		ServerConnection connection = getConnection();
		if (connection != null) {
			try {
				connection.close();
			}
			catch (StoreException e) {
				throw new ResourceException(e);
			}
		}
	}
}
