/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

/**
 * HTTP-related exception that includes the relevant HTTP status code.
 * 
 * @author Arjohn Kampman
 */
public class HTTPException extends Exception {

	private static final long serialVersionUID = 1356463348553827230L;

	public static HTTPException create(int statusCode, String msg) {
		if (ClientHTTPException.isInRange(statusCode)) {
			return ClientHTTPException.create(statusCode, msg);
		}
		else if (ServerHTTPException.isInRange(statusCode)) {
			return ServerHTTPException.create(statusCode, msg);
		}
		else {
			return new HTTPException(statusCode, msg);
		}
	}

	private int statusCode;

	public HTTPException(int statusCode) {
		super();
		setStatusCode(statusCode);
	}

	public HTTPException(int statusCode, String message) {
		super(message);
		setStatusCode(statusCode);
	}

	public HTTPException(int statusCode, String message, Throwable t) {
		super(message, t);
		setStatusCode(statusCode);
	}

	public HTTPException(int statusCode, Throwable t) {
		super(t);
		setStatusCode(statusCode);
	}

	public final int getStatusCode() {
		return statusCode;
	}

	protected void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
}
