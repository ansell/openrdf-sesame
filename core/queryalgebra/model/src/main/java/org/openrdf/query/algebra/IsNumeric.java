/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * IsNumeric - Boolean operator determining if the supplied expression
 * represents a numeric value.
 * 
 * @author Jeen
 */
public class IsNumeric extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsNumeric() {
	}

	public IsNumeric(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof IsNumeric && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "IsNumeric".hashCode();
	}

	@Override
	public IsNumeric clone() {
		return (IsNumeric)super.clone();
	}
}
