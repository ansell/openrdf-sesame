/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.cas;

/**
 * @author Arjohn Kampman
 */
public class ProxyFailure implements ServiceResponse {

	/**
	 * Not all of the required request parameters were present.
	 */
	public static final String INVALID_REQUEST = "INVALID_REQUEST";

	/**
	 * The pgt provided was invalid.
	 */
	public static final String BAD_PGT = "BAD_PGT";

	/**
	 * An internal error occurred during ticket validation.
	 */
	public static final String INTERNAL_ERROR = "INTERNAL_ERROR";

	private final String code;

	private final String message;

	ProxyFailure(String code, String message) {
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