/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

public class NotFound extends ClientHTTPException {

	private static final long serialVersionUID = -6502236504802699855L;

	static final int STATUS_CODE = HttpURLConnection.HTTP_NOT_FOUND;

	/**
	 * Creates a {@link NotFound} with status code 404 Not Found.
	 */
	public NotFound(String msg) {
		super(STATUS_CODE, msg);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode != STATUS_CODE) {
			throw new IllegalArgumentException("Status code must be 404, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
