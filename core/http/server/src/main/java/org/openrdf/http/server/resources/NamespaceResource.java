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

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class NamespaceResource extends SesameResource {

	public static final String NS_PREFIX_PARAM = "ns_prefix";

	private String prefix;

	protected void doInit() {
		super.doInit();
		prefix = (String)getRequest().getAttributes().get(NS_PREFIX_PARAM);
		addCacheableMediaTypes(MediaType.TEXT_PLAIN);
	}

	@Override
	protected Representation get(Variant variant)
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
	protected Representation put(Representation entity, Variant variant)
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

				ServerConnection connection = getConnection();
				connection.setNamespace(prefix, namespace);
				connection.getCacheInfo().processUpdate();

				getResponse().setStatus(SUCCESS_NO_CONTENT);
				return null;
			}
			catch (IOException e) {
				throw new ResourceException(e);
			}
			catch (StoreException e) {
				throw new ResourceException(e);
			}
		}
		else {
			throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
		}
	}

	@Override
	protected Representation delete(Variant variant)
		throws ResourceException
	{
		try {
			ServerConnection connection = getConnection();
			connection.removeNamespace(prefix);
			connection.getCacheInfo().processUpdate();

			getResponse().setStatus(SUCCESS_NO_CONTENT);
			return null;
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
	}
}
