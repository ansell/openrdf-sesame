/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import org.openrdf.http.server.resources.helpers.SesameResource;

/**
 * @author Arjohn Kampman
 */
public class PingConnectionResource extends SesameResource {

	public PingConnectionResource(Context context, Request request, Response response) {
		super(context, request, response);
		this.setReadable(false);
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		// ignore, last access time has been updated by ConnectionResolver
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
	}
}
