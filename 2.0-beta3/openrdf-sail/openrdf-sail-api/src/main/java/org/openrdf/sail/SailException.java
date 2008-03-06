/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

import org.openrdf.OpenRDFException;

/**
 * An exception thrown by some methods in Sail to indicate that a requested
 * operation could not be executed.
 */
public class SailException extends OpenRDFException {

	private static final long serialVersionUID = 2432600780159917763L;

	public SailException() {
		super();
	}

	public SailException(String msg) {
		super(msg);
	}

	public SailException(Throwable t) {
		super(t);
	}

	public SailException(String msg, Throwable t) {
		super(msg, t);
	}
}
