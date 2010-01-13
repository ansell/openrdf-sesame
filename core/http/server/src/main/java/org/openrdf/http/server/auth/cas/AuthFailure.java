/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.auth.cas;

/**
 * @author Arjohn Kampman
 */
class AuthFailure implements ServiceResponse {

	/**
	 * Not all of the required request parameters were present.
	 */
	static final String INVALID_REQUEST = "INVALID_REQUEST";

	/**
	 * The ticket provided was not valid, or the ticket did not come from an
	 * initial login and "renew" was set on validation.
	 */
	static final String INVALID_TICKET = "INVALID_TICKET";

	/**
	 * The ticket provided was valid, but the service specified did not match the
	 * service associated with the ticket.
	 */
	static final String INVALID_SERVICE = "INVALID_SERVICE";

	/**
	 * An internal error occurred during ticket validation.
	 */
	static final String INTERNAL_ERROR = "INTERNAL_ERROR";

	final String code;

	final String message;

	AuthFailure(String code, String message) {
		this.code = code;
		this.message = message;
	}

	@Override
	public String toString() {
		return "[" + code + "] " + message;
	}
}