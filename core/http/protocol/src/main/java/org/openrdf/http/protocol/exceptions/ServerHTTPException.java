/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

/**
 * HTTP-related exception indicating that an error occurred in a server or the
 * server is incapable of performing the request. Status codes for these types
 * of errors are in the 5xx range. The default status code for constructors
 * without a <tt>statusCode</tt> parameter is <tt>500 Internal Server Error</tt>
 * .
 * 
 * @author Arjohn Kampman
 */
public class ServerHTTPException extends HTTPException {

	private static final long serialVersionUID = -3949837199542648966L;

	private static final int DEFAULT_STATUS_CODE = HttpURLConnection.HTTP_INTERNAL_ERROR;

	public static ServerHTTPException create(int statusCode, String msg) {
		return new ServerHTTPException(statusCode, msg);
	}

	public static boolean isInRange(int statusCode) {
		return statusCode >= 500 && statusCode <= 599;
	}

	/**
	 * Creates a {@link ServerHTTPException} with status code 500 "Internal
	 * Server Error".
	 */
	public ServerHTTPException() {
		this(DEFAULT_STATUS_CODE);
	}

	/**
	 * Creates a {@link ServerHTTPException} with status code 500 "Internal
	 * Server Error".
	 */
	public ServerHTTPException(String msg) {
		this(DEFAULT_STATUS_CODE, msg);
	}

	/**
	 * Creates a {@link ServerHTTPException} with status code 500 "Internal
	 * Server Error".
	 */
	public ServerHTTPException(String msg, Throwable t) {
		this(DEFAULT_STATUS_CODE, t);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode) {
		super(statusCode);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode, String message) {
		super(statusCode, message);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode, String message, Throwable t) {
		super(statusCode, message, t);
	}

	/**
	 * Creates a {@link ServerHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 5xx range.
	 */
	public ServerHTTPException(int statusCode, Throwable t) {
		super(statusCode, t);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (!isInRange(statusCode)) {
			throw new IllegalArgumentException("Status code must be in the 5xx range, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
