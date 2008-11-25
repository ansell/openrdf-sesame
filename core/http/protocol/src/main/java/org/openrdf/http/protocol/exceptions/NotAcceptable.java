/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

public class NotAcceptable extends ClientHTTPException {

	private static final long serialVersionUID = 8540515029592553577L;

	static final int STATUS_CODE = HttpURLConnection.HTTP_NOT_ACCEPTABLE;

	/**
	 * Creates a {@link NotAcceptable} with status code 406 Not Acceptable.
	 */
	public NotAcceptable(String msg) {
		super(STATUS_CODE, msg);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode != STATUS_CODE) {
			throw new IllegalArgumentException("Status code must be 406, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
