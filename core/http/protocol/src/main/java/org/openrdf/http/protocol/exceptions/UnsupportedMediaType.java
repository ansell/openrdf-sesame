/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

/**
 * Thrown when the server cannot parse the request body.
 * 
 * @author James Leigh
 */
public class UnsupportedMediaType extends ClientHTTPException {

	private static final long serialVersionUID = 8540515029592553577L;

	static final int STATUS_CODE = HttpURLConnection.HTTP_UNSUPPORTED_TYPE;

	/**
	 * Creates a {@link UnsupportedMediaType} with status code 415 Unsupported
	 * Media Type.
	 */
	public UnsupportedMediaType(String msg) {
		super(STATUS_CODE, msg);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode != STATUS_CODE) {
			throw new IllegalArgumentException("Status code must be 415, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
