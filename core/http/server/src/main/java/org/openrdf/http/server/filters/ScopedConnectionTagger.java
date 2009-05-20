/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.util.WrapperRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.helpers.ServerRepository;
import org.openrdf.store.StoreException;

/**
 * "Tags" requests that pass through this filter by adding a new
 * {@link ServerConnection} for the {@link RequestAtt#getRepository(Request)
 * request's repository} to the request's attributes, and closes the connection
 * after the request has been processed.
 * 
 * @author Arjohn Kampman
 */
public class ScopedConnectionTagger extends Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ScopedConnectionTagger(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		ServerRepository repository = RequestAtt.getRepository(request);

		try {
			ServerConnection connection = repository.getConnection();
			connection.addRequest(request);
			RequestAtt.setConnection(request, connection);
			return Filter.CONTINUE;
		}
		catch (StoreException e) {
			logger.error("Failed to open a connection on the repository", e);
			response.setStatus(SERVER_ERROR_INTERNAL, e, "Repository error: " + e.getMessage());
			return Filter.STOP;
		}
	}

	@Override
	protected void afterHandle(Request request, Response response) {
		Representation entity = response.getEntity();

		if (entity != null) {
			entity = new ScopedConnectionRepresentation(entity, request);
			response.setEntity(entity);
		}
		else {
			closeConnection(request);
		}
	}

	private void closeConnection(Request request) {
		try {
			ServerConnection connection = RequestAtt.getConnection(request);
			connection.removeRequest(request);
			connection.close();
		}
		catch (StoreException e) {
			logger.error("Failed to close repository connection", e);
		}
	}

	private class ScopedConnectionRepresentation extends WrapperRepresentation {

		private final Request request;

		public ScopedConnectionRepresentation(Representation wrappedRepresentation, Request request) {
			super(wrappedRepresentation);
			this.request = request;
		}

		@Override
		public void release() {
			super.release();
			closeConnection(request);
		}
	}
}
