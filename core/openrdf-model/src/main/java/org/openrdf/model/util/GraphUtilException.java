/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import org.openrdf.OpenRDFException;

/**
 * An exception thrown by {@link GraphUtil} when specific conditions are not
 * met.
 * 
 * @author Arjohn Kampman
 */
public class GraphUtilException extends OpenRDFException {

	private static final long serialVersionUID = 3886967415616842867L;

	public GraphUtilException() {
		super();
	}

	public GraphUtilException(String message) {
		super(message);
	}

	public GraphUtilException(Throwable t) {
		super(t);
	}

	public GraphUtilException(String message, Throwable t) {
		super(message, t);
	}
}
