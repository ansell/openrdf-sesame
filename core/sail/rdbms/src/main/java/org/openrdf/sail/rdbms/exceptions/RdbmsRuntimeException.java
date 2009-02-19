/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.exceptions;


/**
 * Thrown when no exception is declared.
 * 
 * @author James Leigh
 * 
 */
public class RdbmsRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 9001754124114839089L;

	public RdbmsRuntimeException(InterruptedException e) {
		super(e);
	}

}
