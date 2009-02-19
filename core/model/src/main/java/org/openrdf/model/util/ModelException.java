/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import org.openrdf.model.Model;


/**
 * An exception thrown by {@link Model} and {@link ModelUtil} when specific conditions are not
 * met.
 * 
 * @author Arjohn Kampman
 */
public class ModelException extends RuntimeException {

	private static final long serialVersionUID = 3886967415616842867L;

	public ModelException() {
		super();
	}

	public ModelException(String message) {
		super(message);
	}

	public ModelException(Throwable t) {
		super(t);
	}

	public ModelException(String message, Throwable t) {
		super(message, t);
	}
}
