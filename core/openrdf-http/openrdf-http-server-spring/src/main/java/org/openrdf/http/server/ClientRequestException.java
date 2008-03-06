/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import org.openrdf.http.protocol.error.ExceptionInfo;


/**
 * HTTP-related exception indicating that a client has sent a bad request. The
 * exception contains a status code indicating the type of error.
 * 
 * @author Arjohn Kampman
 */
public class ClientRequestException extends Exception {

	private static final long serialVersionUID = 7722604284325312749L;

	private int statusCode;

	private ExceptionInfo exceptionInfo;

	public ClientRequestException(int statusCode, String msg) {
		this(statusCode, msg, null);
	}

	public ClientRequestException(int statusCode, String msg, Throwable t) {
		this(statusCode, msg, t, null);
	}

	public ClientRequestException(int statusCode, String msg, Throwable t, ExceptionInfo exceptionInfo) {
		super(msg, t);
		this.statusCode = statusCode;
		this.exceptionInfo = exceptionInfo;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public boolean hasExceptionInfo() {
		return exceptionInfo != null;
	}

	public ExceptionInfo getExceptionInfo() {
		return exceptionInfo;
	}
}
