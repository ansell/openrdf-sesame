/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;




/**
 * A boolean constant that can represent <tt>true</tt> and <tt>false</tt>.
 */
public class BooleanConstant extends BooleanExpr {

	/*-----------*
	 * Constants *
	 *-----------*/

	/**
	 * The boolean value <tt>true</tt>.
	 */
	public static final BooleanConstant TRUE = new BooleanConstant(true);

	/**
	 * The boolean value <tt>false</tt>.
	 */
	public static final BooleanConstant FALSE = new BooleanConstant(false);

	/*-----------*
	 * Variables *
	 *-----------*/

	private boolean _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BooleanConstant(boolean value) {
		_value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public boolean isTrue() {
		return _value;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return String.valueOf(_value);
	}
}
