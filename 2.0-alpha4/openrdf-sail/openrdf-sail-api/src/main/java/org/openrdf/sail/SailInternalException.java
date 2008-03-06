/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail;

/**
 * A runtime exception that can be used to indicate an error or an
 * unexpected situation in an RDF Sail internally. E.g.: the database
 * to connect to does not exist.
 */
public class SailInternalException extends RuntimeException {

	private static final long serialVersionUID = -6638944442352563368L;

	public SailInternalException() {
		super();
	}

	public SailInternalException(String msg) {
		super(msg);
	}

	public SailInternalException(Throwable t) {
		super(t);
	}

	public SailInternalException(String msg, Throwable t) {
		super(msg, t);
	}
}
