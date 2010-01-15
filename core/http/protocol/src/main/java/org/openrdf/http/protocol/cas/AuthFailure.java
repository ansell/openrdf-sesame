/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.cas;

/**
 * @author Arjohn Kampman
 */
public class AuthFailure implements ServiceResponse {

	/**
	 * Not all of the required request parameters were present.
	 */
	public static final String INVALID_REQUEST = "INVALID_REQUEST";

	/**
	 * The ticket provided was not valid, or the ticket did not come from an
	 * initial login and "renew" was set on validation.
	 */
	public static final String INVALID_TICKET = "INVALID_TICKET";

	/**
	 * The ticket provided was valid, but the service specified did not match the
	 * service associated with the ticket.
	 */
	public static final String INVALID_SERVICE = "INVALID_SERVICE";

	/**
	 * An internal error occurred during ticket validation.
	 */
	public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

	private final String code;

	private final String message;

	AuthFailure(String code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return "[" + code + "] " + message;
	}
}