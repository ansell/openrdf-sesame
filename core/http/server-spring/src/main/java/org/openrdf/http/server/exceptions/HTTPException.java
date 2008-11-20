/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.exceptions;

/**
 * HTTP-related exception that includes the relevant HTTP status code.
 * 
 * @author Arjohn Kampman
 */
public class HTTPException extends Exception {

	private static final long serialVersionUID = 1356463348553827230L;

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
