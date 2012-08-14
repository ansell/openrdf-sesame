/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.exceptions;

import javax.servlet.ServletException;

public class BadRequestException extends ServletException {
	private static final long serialVersionUID = -6227037493079059474L;

	public BadRequestException(String message) {
		super(message);
	}

	public BadRequestException(String message, Throwable rootCause) {
		super(message, rootCause);
	}
}
