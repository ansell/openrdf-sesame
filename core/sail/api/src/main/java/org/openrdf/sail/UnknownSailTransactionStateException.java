/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * Indicates that a SAIL's transaction state (active or inactive) cannot be determined.
 * 
 * @since 2.7.0
 * @author Jeen Broekstra
 */
public class UnknownSailTransactionStateException extends SailException {

	private static final long serialVersionUID = 8616609700552763681L;

	public UnknownSailTransactionStateException(String msg) {
		super(msg);
	}

}
