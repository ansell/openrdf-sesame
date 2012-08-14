/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Checks RDF term equality.
 */
public class SameTerm extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SameTerm() {
	}

	public SameTerm(ValueExpr leftArg, ValueExpr rightArg) {
		super(leftArg, rightArg);
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
		return other instanceof SameTerm && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "SameTerm".hashCode();
	}

	@Override
	public SameTerm clone() {
		return (SameTerm)super.clone();
	}
}
