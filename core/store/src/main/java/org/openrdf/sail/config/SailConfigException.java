/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.config;

import org.openrdf.OpenRDFException;

/**
 * Exception indicating a sail configuration problem.
 * 
 * @author Arjohn Kampman
 */
public abstract class SailConfigException extends OpenRDFException {

	private static final long serialVersionUID = 185213210952981723L;

	public SailConfigException() {
		super();
	}

	public SailConfigException(String message) {
		super(message);
	}

	public SailConfigException(Throwable t) {
		super(t);
	}

	public SailConfigException(String message, Throwable t) {
		super(message, t);
	}
}
