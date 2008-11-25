/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

public class Unauthorized extends ClientHTTPException {

	private static final long serialVersionUID = -5620769753019231519L;

	static final int STATUS_CODE = HttpURLConnection.HTTP_UNAUTHORIZED;

	/**
	 * Creates a {@link Unauthorized} with status code 401:
	 * Unauthorized.
	 */
	public Unauthorized(String msg) {
		super(STATUS_CODE, msg);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode != STATUS_CODE) {
			throw new IllegalArgumentException("Status code must be 401, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
