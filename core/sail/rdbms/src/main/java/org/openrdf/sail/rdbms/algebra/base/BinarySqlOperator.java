/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;


/**
 * An abstract binary sql operator with two arguments.
 * 
 * @author James Leigh
 * 
 */
public abstract class BinarySqlOperator extends NarySqlOperator {

	public BinarySqlOperator() {
		super();
	}

	public BinarySqlOperator(SqlExpr leftArg, SqlExpr rightArg) {
		super(leftArg, rightArg);
	}

	public SqlExpr getLeftArg() {
		return getArg(0);
	}

	public void setLeftArg(SqlExpr leftArg) {
		this.setArg(0, leftArg);
	}

	public SqlExpr getRightArg() {
		return getArg(1);
	}

	public void setRightArg(SqlExpr rightArg) {
		this.setArg(1, rightArg);
	}

	@Override
	public BinarySqlOperator clone() {
		return (BinarySqlOperator)super.clone();
	}
}
