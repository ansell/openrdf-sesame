/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ServerResource;

import org.openrdf.http.protocol.Protocol;

/**
 * @author Arjohn Kampman
 */
public class ProtocolResource extends ServerResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
		setConditional(false);
	}

	@Override
	protected Representation get() {
		return new StringRepresentation(Protocol.VERSION);
	}
}
