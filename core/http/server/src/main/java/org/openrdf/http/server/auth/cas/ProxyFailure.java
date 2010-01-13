/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.auth.cas;

/**
 * @author Arjohn Kampman
 */
class ProxyFailure implements ServiceResponse {

	/**
	 * Not all of the required request parameters were present.
	 */
	static final String INVALID_REQUEST = "INVALID_REQUEST";

	/**
	 * The pgt provided was invalid.
	 */
	static final String BAD_PGT = "BAD_PGT";

	/**
	 * An internal error occurred during ticket validation.
	 */
	static final String INTERNAL_ERROR = "INTERNAL_ERROR";

	final String code;

	final String message;

	ProxyFailure(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "[" + code + "] " + message;
	}
}