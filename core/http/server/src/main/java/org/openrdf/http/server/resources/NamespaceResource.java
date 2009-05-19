/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND;
import static org.restlet.data.Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;
import static org.restlet.data.Status.SUCCESS_NO_CONTENT;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class NamespaceResource extends SesameResource {

	public static final String NS_PREFIX_PARAM = "ns_prefix";

	private final String prefix;

	public NamespaceResource(Context context, Request request, Response response) {
		super(context, request, response);

		prefix = (String)request.getAttributes().get(NS_PREFIX_PARAM);

		addCacheableVariants(new Variant(MediaType.TEXT_PLAIN));
	}

	@Override
	public Representation represent(Variant variant)
		throws ResourceException
	{
		getResponse().setDimensions(ServerUtil.VARY_ACCEPT);

		try {
			String namespace = getConnection().getNamespace(prefix);

			if (namespace == null) {
				throw new ResourceException(CLIENT_ERROR_NOT_FOUND, "Undefined prefix: " + prefix);
			}

			if (variant.getMediaType().equals(MediaType.TEXT_PLAIN, true)) {
				return new StringRepresentation(namespace);
			}

			throw new ResourceException(SERVER_ERROR_INTERNAL, "Unsupported media type: "
					+ variant.getMediaType());
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	public void storeRepresentation(Representation entity)
		throws ResourceException
	{
		if (MediaType.TEXT_PLAIN.equals(entity.getMediaType(), true)) {
			try {
				String namespace = entity.getText().trim();

				if (namespace.length() == 0) {
					throw new ResourceException(CLIENT_ERROR_BAD_REQUEST,
							"No namespace name found in request body");
				}
				// FIXME: perform some sanity checks on the namespace string

				getConnection().setNamespace(prefix, namespace);
				getResponse().setStatus(SUCCESS_NO_CONTENT);
			}
			catch (IOException e) {
				throw new ResourceException(e);
			}
			catch (StoreException e) {
				throw new ResourceException(e);
			}
		}

		throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
	}

	@Override
	public void removeRepresentations()
		throws ResourceException
	{
		try {
			getConnection().removeNamespace(prefix);
			getResponse().setStatus(SUCCESS_NO_CONTENT);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}
}
