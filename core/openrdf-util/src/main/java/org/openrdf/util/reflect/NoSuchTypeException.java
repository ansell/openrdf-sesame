/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.util.reflect;

/**
 * Thrown by {@link KeyedObjectFactory} to indicate that a specified key is not
 * associated with any type.
 */
public class NoSuchTypeException extends Exception {

	private static final long serialVersionUID = 168377567276040338L;

	/**
	 * Creates a new NoSuchTypeException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public NoSuchTypeException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new NoSuchTypeException.
	 * 
	 * @param cause
	 *        The cause of the exception.
	 */
	public NoSuchTypeException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new NoSuchTypeException wrapping another exception.
	 * 
	 * @param msg
	 *        An error message.
	 * @param cause
	 *        The cause of the exception.
	 */
	public NoSuchTypeException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
