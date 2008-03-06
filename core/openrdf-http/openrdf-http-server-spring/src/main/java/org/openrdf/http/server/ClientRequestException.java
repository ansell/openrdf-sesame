/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import javax.servlet.ServletException;

/**
 * HTTP-related exception indicating that a client has sent a bad request. The
 * exception contains a status code indicating the type of error.
 * 
 * @author Arjohn Kampman
 */
public class ClientRequestException extends ServletException {

	private static final long serialVersionUID = 7722604284325312749L;

	private int statusCode;

	public ClientRequestException(int statusCode, String msg) {
		super(msg);
		this.statusCode = statusCode;
	}

	public ClientRequestException(int statusCode, String msg, Throwable t) {
		super(msg, t);
		this.statusCode = statusCode;
	}

	public int getStatusCode() {
		return statusCode;
	}
}
