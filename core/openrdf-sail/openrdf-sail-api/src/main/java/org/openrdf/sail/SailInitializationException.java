/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * An exception thrown to indicate that the Sail could not be initialized.
 * 
 * @deprecated Sail API no longer distinguishes between initialization
 *             exceptions and other Sail exceptions.
 */
@Deprecated
public class SailInitializationException extends SailException {

	private static final long serialVersionUID = -3807295645245475143L;

	public SailInitializationException() {
		super();
	}

	public SailInitializationException(String msg) {
		super(msg);
	}

	public SailInitializationException(Throwable t) {
		super(t);
	}

	public SailInitializationException(String msg, Throwable t) {
		super(msg, t);
	}
}
