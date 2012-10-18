/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

/**
 * A specific subtype of {@link RepositoryException} that indicates the
 * connection's transaction state can not be determined.
 * 
 * @since 2.7.0.
 * @author Jeen Broekstra
 */
public class UnknownTransactionStateException extends RepositoryException {

	private static final long serialVersionUID = -5938676154783704438L;

	public UnknownTransactionStateException() {
		super();
	}

	public UnknownTransactionStateException(String msg) {
		super(msg);
	}

	public UnknownTransactionStateException(Throwable t) {
		super(t);
	}

	public UnknownTransactionStateException(String msg, Throwable t) {
		super(msg, t);
	}
}
