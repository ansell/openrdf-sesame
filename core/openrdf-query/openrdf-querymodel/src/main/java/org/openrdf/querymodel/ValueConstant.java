/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


import org.openrdf.model.Value;


/**
 * A ValueExpr with a constant value.
 */
public class ValueConstant extends ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Value _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueConstant(Value value) {
		_value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/
	
	public Value getValue() {
		return _value;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append("VALUE_CONSTANT (value=");
		sb.append(_value.toString());
		sb.append(")");

		return sb.toString();
	}
}
