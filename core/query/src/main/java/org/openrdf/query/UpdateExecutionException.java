/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import org.openrdf.OpenRDFException;

/**
 * An exception indicating that the execution of an update failed.
 * 
 * @author Jeen
 */
public class UpdateExecutionException extends OpenRDFException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7969399526232927434L;

	public UpdateExecutionException() {
		super();
	}

	/**
	 * Creates a new UpdateExecutionException.
	 * 
	 * @param msg
	 *        An error message.
	 */
	public UpdateExecutionException(String msg) {
		super(msg);
	}

	/**
	 * Creates a new {@link UpdateExecutionException} wrapping another exception.
	 * 
	 * @param cause
	 *        the cause of the exception
	 */
	public UpdateExecutionException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a new {@link UpdateExecutionException} wrapping another exception.
	 * 
	 * @param msg
	 *        and error message.
	 * @param cause
	 *        the cause of the exception
	 */
	public UpdateExecutionException(String msg, Throwable cause) {
		super(msg, cause);
	}

}
