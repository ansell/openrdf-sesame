/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server;

import javax.servlet.ServletException;

/**
 * HTTP-related exception indicating that an unexpected error occurred in a
 * server.
 * 
 * @author Arjohn Kampman
 */
public class InternalServerException extends ServletException {

	private static final long serialVersionUID = -4507565470363588718L;

	public InternalServerException(String msg) {
		super(msg);
	}

	public InternalServerException(String msg, Throwable t) {
		super(msg, t);
	}
}
