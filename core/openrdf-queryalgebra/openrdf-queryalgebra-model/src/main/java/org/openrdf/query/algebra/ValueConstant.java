/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import org.openrdf.model.Value;

/**
 * A ValueExpr with a constant value.
 */
public class ValueConstant extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Value _value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueConstant() {
	}

	public ValueConstant(Value value) {
		_value = value;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Value getValue() {
		return _value;
	}

	public void setValue(Value value) {
		_value = value;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(64);

		sb.append("VALUE_CONSTANT (value=");
		sb.append(_value.toString());
		sb.append(")");

		return sb.toString();
	}

	public ValueExpr cloneValueExpr() {
		return new ValueConstant(getValue());
	}
}
