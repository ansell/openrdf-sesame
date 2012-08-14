/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class IsResource extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsResource() {
	}

	public IsResource(ValueExpr arg) {
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
		return other instanceof IsResource && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() & "IsResource".hashCode();
	}

	@Override
	public IsResource clone() {
		return (IsResource)super.clone();
	}
}
