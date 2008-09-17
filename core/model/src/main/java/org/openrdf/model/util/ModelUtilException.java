/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import org.openrdf.OpenRDFException;

/**
 * An exception thrown by {@link ModelUtil} when specific conditions are not
 * met.
 * 
 * @author Arjohn Kampman
 */
public class ModelUtilException extends OpenRDFException {

	private static final long serialVersionUID = 3886967415616842867L;

	public ModelUtilException() {
		super();
	}

	public ModelUtilException(String message) {
		super(message);
	}

	public ModelUtilException(Throwable t) {
		super(t);
	}

	public ModelUtilException(String message, Throwable t) {
		super(message, t);
	}
}
