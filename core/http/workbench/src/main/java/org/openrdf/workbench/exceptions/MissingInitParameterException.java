/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.exceptions;

import javax.servlet.ServletException;

public class MissingInitParameterException extends ServletException {
	private static final long serialVersionUID = 8543657273860596921L;

	public MissingInitParameterException(String parameter) {
		super("Missing parameter: " + parameter);
	}

}
