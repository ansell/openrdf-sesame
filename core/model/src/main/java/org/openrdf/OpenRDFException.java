/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf;

/**
 * General superclass of all checked exceptions that parts of OpenRDF Sesame can
 * throw.
 * 
 * @author jeen
 */
public abstract class OpenRDFException extends Exception {

	private static final long serialVersionUID = 8913366826930181397L;

	public OpenRDFException() {
		super();
	}

	public OpenRDFException(String msg) {
		super(msg);
	}

	public OpenRDFException(Throwable t) {
		super(t);
	}

	public OpenRDFException(String msg, Throwable t) {
		super(msg, t);
	}
}
