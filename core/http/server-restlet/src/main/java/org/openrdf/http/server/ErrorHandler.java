/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import static org.restlet.data.CharacterSet.UTF_8;
import static org.restlet.data.MediaType.TEXT_PLAIN;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;

import org.restlet.data.Language;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.StringRepresentation;
import org.restlet.service.StatusService;

import org.openrdf.http.protocol.error.ErrorInfo;

/**
 * @author Arjohn Kampman
 */
public class ErrorHandler extends StatusService {

	@Override
	public Representation getRepresentation(Status status, Request request, Response response) {
		// Return UTF-8 encoded plain text errors
		return new StringRepresentation(status.getDescription(), TEXT_PLAIN, Language.ALL, UTF_8);
	}

	@Override
	public Status getStatus(Throwable throwable, Request request, Response response) {
		if (throwable instanceof ErrorInfoException) {
			ErrorInfo errInfo = ((ErrorInfoException)throwable).getErrorInfo();
			response.setEntity(new StringRepresentation(errInfo.toString(), TEXT_PLAIN, Language.ALL, UTF_8));
			return new Status(CLIENT_ERROR_BAD_REQUEST, errInfo.getErrorType().getLabel());
		}
		else {
			return super.getStatus(throwable, request, response);
		}
	}
}
