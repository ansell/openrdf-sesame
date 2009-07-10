/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import java.util.Date;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Tag;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;

import org.openrdf.http.protocol.Protocol;

/**
 * @author Arjohn Kampman
 */
public class ProtocolResource extends Resource {

	public ProtocolResource(Context context, Request request, Response response) {
		super(context, request, response);
		setNegotiateContent(false);
		getVariants().add(createVersionEntity());
	}

	private Representation createVersionEntity() {
		Representation entity = new StringRepresentation(Protocol.VERSION);
		entity.setTag(new Tag(Protocol.VERSION, false));
		entity.setModificationDate(new Date());
		return entity;
	}
}
	