/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import java.net.HttpURLConnection;

import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;

public class BadRequest extends ClientHTTPException {

	private static final long serialVersionUID = 8540515029592553577L;

	static final int STATUS_CODE = HttpURLConnection.HTTP_BAD_REQUEST;

	public static BadRequest create(String msg) {
		ErrorInfo errInfo = ErrorInfo.parse(msg);
		ErrorType type = errInfo.getErrorType();
		if (type == ErrorType.UNSUPPORTED_FILE_FORMAT) {
			return new UnsupportedFileFormat(errInfo.getErrorMessage());
		}
		else if (type == ErrorType.UNSUPPORTED_QUERY_LANGUAGE) {
			return new UnsupportedQueryLanguage(errInfo.getErrorMessage());
		}
		else if (type == ErrorType.MALFORMED_DATA) {
			return new MalformedData(errInfo.getErrorMessage());
		}
		else if (type == ErrorType.MALFORMED_QUERY) {
			return new MalformedQuery(errInfo.getErrorMessage());
		}
		else {
			return new BadRequest(errInfo.toString());
		}
	}

	/**
	 * Creates a {@link BadRequest} with status code 400 Bad Request.
	 */
	public BadRequest(String msg) {
		super(STATUS_CODE, msg);
	}

	@Override
	protected void setStatusCode(int statusCode) {
		if (statusCode != STATUS_CODE) {
			throw new IllegalArgumentException("Status code must be 400, is: " + statusCode);
		}

		super.setStatusCode(statusCode);
	}
}
