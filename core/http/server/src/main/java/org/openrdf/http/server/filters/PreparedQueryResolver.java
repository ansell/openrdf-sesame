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
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.routing.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.helpers.ServerConnection;
import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.query.Query;

/**
 * Filter that resolves a {@link #QUERY_ID_PARAM query identifier} to a
 * {@link Query} object and adds this object the a request's attributes. This
 * filter will produce an appropriate HTTP error when the concerning query could
 * not be found.
 * 
 * @author Arjohn Kampman
 */
public class PreparedQueryResolver extends Filter {

	public static final String QUERY_ID_PARAM = "queryID";

	public static String getQueryID(Request request) {
		return (String)request.getAttributes().get(QUERY_ID_PARAM);
	}

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public PreparedQueryResolver(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		String queryID = getQueryID(request);
		logger.debug("{}={}", QUERY_ID_PARAM, queryID);

		ServerConnection connection = RequestAtt.getConnection(request);
		Query query = connection.getQuery(queryID);

		if (query == null) {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return Filter.STOP;
		}

		try {
			// Parse request-specific parameters
			Form params = ServerUtil.getParameters(request);
			if (params != null) {
				QueryParser.parseQueryParameters(query, params, connection.getValueFactory());
			}

			RequestAtt.setQuery(request, query);
			return Filter.CONTINUE;
		}
		catch (ResourceException e) {
			response.setStatus(e.getStatus(), e.getMessage());
			return Filter.STOP;
		}
	}
}
