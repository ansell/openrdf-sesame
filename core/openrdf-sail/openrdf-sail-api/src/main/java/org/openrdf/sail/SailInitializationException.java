/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * An exception thrown by {@link Sail#initialize} to indicate that the Sail
 * could not be initialized.
 */
public class SailInitializationException extends Exception {

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
