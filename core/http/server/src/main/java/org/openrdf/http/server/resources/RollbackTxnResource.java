/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;
import static org.restlet.data.Status.SUCCESS_NO_CONTENT;

import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class RollbackTxnResource extends SesameResource {

	@Override
	protected void doInit() {
		super.doInit();
		setNegotiated(false);
		setConditional(false);
	}

	@Override
	protected Representation post(Representation entity)
		throws ResourceException
	{
		try {
			getConnection().rollback();
			getResponse().setStatus(SUCCESS_NO_CONTENT);
			return null;
		}
		catch (StoreException e) {
			throw new ResourceException(SERVER_ERROR_INTERNAL, e);
		}
	}
}
