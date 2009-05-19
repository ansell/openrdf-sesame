/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;
import static org.restlet.data.Status.SUCCESS_NO_CONTENT;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import org.openrdf.http.server.resources.helpers.SesameResource;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class RollbackTxnResource extends SesameResource {

	public RollbackTxnResource(Context context, Request request, Response response) {
		super(context, request, response);
		this.setReadable(false);
	}

	@Override
	public boolean allowPost() {
		return true;
	}

	@Override
	public void handlePost() {
		try {
			getConnection().rollback();
			getResponse().setStatus(SUCCESS_NO_CONTENT);
		}
		catch (StoreException e) {
			getResponse().setStatus(SERVER_ERROR_INTERNAL, e);
		}
	}
}
