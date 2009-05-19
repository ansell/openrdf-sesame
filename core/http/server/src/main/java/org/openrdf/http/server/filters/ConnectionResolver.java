/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
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
		logger.debug(CONNECTION_ID_PARAM + "={}", connectionID);

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
		ServerConnection connection = RequestAtt.getConnection(request);

		if (connection != null) {
			connection.removeRequest(request);
		}
	}
}
