/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

/**
 * Thrown when the client cannot serialise the request to the server.
 * 
 * @author James Leigh
 */
public class NoCompatibleMediaType extends UnsupportedMediaType {

	private static final long serialVersionUID = 8540515029592553577L;

	/**
	 * Creates a {@link NoCompatibleMediaType} with status code 415 Unsupported
	 * Media Type.
	 */
	public NoCompatibleMediaType(String msg) {
		super(msg);
	}
}
