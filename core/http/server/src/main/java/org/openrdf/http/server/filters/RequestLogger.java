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
import org.restlet.representation.Representation;
import org.restlet.routing.Filter;
import org.restlet.util.WrapperRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs the duration of each request.
 * 
 * @author Arjohn Kampman
 */
public class RequestLogger extends Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String START_TIME_ATT = "org.openrdf.sesame.startTime";

	public RequestLogger(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		request.getAttributes().put(START_TIME_ATT, System.nanoTime());
		return Filter.CONTINUE;
	}

	@Override
	protected void afterHandle(Request request, Response response) {
		Representation entity = response.getEntity();

		if (entity != null) {
			entity = new LoggerRepresentation(entity, request);
			response.setEntity(entity);
		}
		else {
			logRequest(request);
		}

		request.getAttributes().put(START_TIME_ATT, System.nanoTime());
	}

	private void logRequest(Request request) {
		long startTime = (Long)request.getAttributes().get(START_TIME_ATT);
		long duration = System.nanoTime() - startTime;
		logger.info("{} {}?{} processed in {} ns", new Object[] {request.getMethod(), request.getResourceRef().getPath(), request.getResourceRef().getQuery(), duration});
	}

	private class LoggerRepresentation extends WrapperRepresentation {

		private final Request request;

		public LoggerRepresentation(Representation wrappedRepresentation, Request request) {
			super(wrappedRepresentation);
			this.request = request;
		}

		@Override
		public void release() {
			super.release();
			logRequest(request);
		}
	}
}
