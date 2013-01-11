/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model.util;

import org.openrdf.model.Model;
import org.openrdf.model.Value;

/**
 * An exception thrown by {@link Model} and {@link ModelUtil} when specific
 * conditions are not met.
 * 
 * @author Arjohn Kampman
 */
public class ModelException extends RuntimeException {

	private static final long serialVersionUID = 3886967415616842867L;

	public ModelException(Value value) {
		this("Unexpected object term: " + value);
	}

	public ModelException(Value v1, Value v2) {
		this(buildMessage(v1, v2));
	}

	public ModelException(String message, Throwable cause) {
		super(message, cause);
	}

	public ModelException(String message) {
		super(message);
	}

	public ModelException(Throwable cause) {
		super(cause);
	}

	private static String buildMessage(Value v1, Value v2) {
		StringBuilder sb = new StringBuilder();
		if (!v1.toString().equals(v2.toString())) {
			sb.append("Object is both ");
			sb.append(v1.toString());
			sb.append(" and ");
			sb.append(v2.toString());
		} else if (!v1.getClass().getName().equals(v2.getClass().getName())) {
			sb.append("Object is both ");
			sb.append("a ");
			sb.append(v1.getClass().getName());
			sb.append(" and a ");
			sb.append(v2.getClass().getName());
		} else {
			sb.append("Object is ");
			sb.append(v1);
			sb.append(" twice!? (store maybe corrupt)");
		}
		return sb.toString();
	}
}
