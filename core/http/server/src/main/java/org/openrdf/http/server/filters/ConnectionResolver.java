/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;
import org.restlet.util.WrapperRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.repository.RepositoryConnection;

/**
 * Filter that resolves a {@link #CONNECTION_ID_PARAM connection identifier} to
 * a {@link RepositoryConnection} object and adds this object the a request's
 * attributes. This filter will produce an appropriate HTTP error when the
 * concerning connection could not be found.
 * 
 * @author Arjohn Kampman
 */
public class ConnectionResolver extends Filter {

	public static final String CONNECTION_ID_PARAM = "connectionID";

	public static String getConnectionID(Request request) {
		return (String)request.getAttributes().get(CONNECTION_ID_PARAM);
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public ConnectionResolver(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		String connectionID = getConnectionID(request);
		logger.debug("{}={}", CONNECTION_ID_PARAM, connectionID);

		ServerConnection connection = RequestAtt.getRepository(request).getConnection(connectionID);

		if (connection != null) {
			connection.addRequest(request);
			RequestAtt.setConnection(request, connection);
			return Filter.CONTINUE;
		}
		else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return Filter.STOP;
		}
	}

	@Override
	protected void afterHandle(Request request, Response response) {
		Representation entity = response.getEntity();

		if (entity != null) {
			entity = new ConnectionRepresentation(entity, request);
			response.setEntity(entity);
		}
		else {
			releaseConnection(request);
		}
	}

	private void releaseConnection(Request request) {
		ServerConnection connection = RequestAtt.getConnection(request);
		connection.removeRequest(request);
	}

	private class ConnectionRepresentation extends WrapperRepresentation {

		private final Request request;

		public ConnectionRepresentation(Representation wrappedRepresentation, Request request) {
			super(wrappedRepresentation);
			this.request = request;
		}

		@Override
		public void release() {
			super.release();
			releaseConnection(request);
		}
	}
}
