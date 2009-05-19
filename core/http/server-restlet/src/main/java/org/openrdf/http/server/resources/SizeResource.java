/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.openrdf.http.protocol.Protocol.CONTEXT_PARAM_NAME;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.model.Resource;
import org.openrdf.model.ValueFactory;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class SizeResource extends SesameResource {

	public SizeResource(Context context, Request request, Response response) {
		super(context, request, response);
		setNegotiateContent(false);
		addCacheableVariants(new Variant(MediaType.TEXT_PLAIN));
	}

	@Override
	public Representation represent(Variant variant)
		throws ResourceException
	{
		getResponse().setDimensions(ServerUtil.VARY_ACCEPT);

		if (variant.getMediaType().equals(MediaType.TEXT_PLAIN, true)) {
			ServerConnection connection = getConnection();
			ValueFactory vf = connection.getValueFactory();
			Resource[] contexts = ServerUtil.parseContextParam(this.getQuery(), CONTEXT_PARAM_NAME, vf);

			try {
				long size = connection.size(contexts);
				return new StringRepresentation(String.valueOf(size));
			}
			catch (StoreException e) {
				throw new ResourceException(e);
			}
		}

		throw new ResourceException(SERVER_ERROR_INTERNAL, "Unsupported media type: " + variant.getMediaType());
	}
}
