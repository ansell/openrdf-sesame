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
		logger.debug(QUERY_ID_PARAM + "={}", queryID);

		Query query = RequestAtt.getConnection(request).getQuery(queryID);

		if (query != null) {
			RequestAtt.setQuery(request, query);
			return Filter.CONTINUE;
		}
		else {
			response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return Filter.STOP;
		}
	}
}
