/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.workbench;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;

import org.openrdf.http.server.ClientHTTPException;


/**
 *
 * @author James Leigh
 */
public class BadRequestException extends ClientHTTPException {
	private static final long serialVersionUID = 1527939038651267283L;

	public BadRequestException(String message) {
		super(SC_BAD_REQUEST, message);
	}

	public BadRequestException(String message, Throwable rootCause) {
		super(SC_BAD_REQUEST, message, rootCause);
	}

}
