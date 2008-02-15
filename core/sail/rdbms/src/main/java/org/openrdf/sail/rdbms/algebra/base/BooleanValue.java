/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

/**
 * A boolean value of true or false.
 * 
 * @author James Leigh
 * 
 */
public abstract class BooleanValue extends SqlConstant<Boolean> {

	public BooleanValue(boolean value) {
		super(value);
	}
}
