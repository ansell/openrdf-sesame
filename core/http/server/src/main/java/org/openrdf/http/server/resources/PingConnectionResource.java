/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.data.Status;
import org.restlet.representation.Representation;

import org.openrdf.http.server.resources.helpers.SesameResource;

/**
 * @author Arjohn Kampman
 */
public class PingConnectionResource extends SesameResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
		setConditional(false);
	}

	@Override
	protected Representation post(Representation entity) {
		// ignore, last access time has been updated by ConnectionResolver
		getResponse().setStatus(Status.SUCCESS_NO_CONTENT);
		return null;
	}
}
