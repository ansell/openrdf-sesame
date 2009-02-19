/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.protocol.exceptions;

import org.openrdf.OpenRDFException;

/**
 * Thrown when the client cannot serialise the request to the server or parse
 * the result from the server.
 * 
 * @author James Leigh
 */
public class NoCompatibleMediaType extends OpenRDFException {

	private static final long serialVersionUID = 8540515029592553577L;

	public NoCompatibleMediaType(String msg) {
		super(msg);
	}
}
