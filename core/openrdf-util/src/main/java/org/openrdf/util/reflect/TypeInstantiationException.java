/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.reflect;

/**
 * Thrown by {@link KeyedObjectFactory} to indicate that it failed to create an
 * instance of a certain type. The cause of the error is wrapped inside this
 * exception.
 */
public class TypeInstantiationException extends Exception {

	private static final long serialVersionUID = -6121394990049441800L;

	/**
	 * Creates a new TypeInstantiationException.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public TypeInstantiationException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new TypeInstantiationException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public TypeInstantiationException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
