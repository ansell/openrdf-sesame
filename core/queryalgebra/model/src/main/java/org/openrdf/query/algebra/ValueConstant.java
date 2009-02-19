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

	private static final long serialVersionUID = -2807539958335282003L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Value value;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public ValueConstant() {
	}

	public ValueConstant(Value value) {
		setValue(value);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		assert value != null : "value must not be null";
		this.value = value;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature()
	{
		StringBuilder sb = new StringBuilder(64);

		sb.append(super.getSignature());
		sb.append(" (value=");
		sb.append(value.toString());
		sb.append(")");

		return sb.toString();
	}

	@Override
	public ValueConstant clone() {
		return (ValueConstant)super.clone();
	}
}
