/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;


/**
 * HTTP-related exception indicating that an HTTP client has erred. Status codes
 * for these types of errors are in the 4xx range. The default status code for
 * constructors without a <tt>statusCode</tt> parameter is
 * <tt>400 Bad Request</tt>.
 * 
 * @author Arjohn Kampman
 */
public class ClientHTTPException extends HTTPException {

	private static final long serialVersionUID = 7722604284325312749L;

	public static ClientHTTPException create(int statusCode, String msg) {
		switch (statusCode) {
			case UnsupportedMediaType.STATUS_CODE:
				return new UnsupportedMediaType(msg);
			case NotFound.STATUS_CODE:
				return new NotFound(msg);
			case Unauthorized.STATUS_CODE:
				return new Unauthorized(msg);
			case BadRequest.STATUS_CODE:
				return BadRequest.create(msg);
			default:
				return new ClientHTTPException(statusCode, msg);
		}
	}

	public static boolean isInRange(int statusCode) {
		return statusCode >= 400 && statusCode <= 499;
	}

	/**
	 * Creates a {@link ClientHTTPException} with the specified status code.
	 * 
	 * @throws IllegalArgumentException
	 *         If <tt>statusCode</tt> is not in the 4xx range.
	 */
	public ClientHTTPException(int statusCode, String message) {
		super(statusCode, message);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (!isInRange(statusCode)) {
			throw new IllegalArgumentException("Status code must be in the 4xx range, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
