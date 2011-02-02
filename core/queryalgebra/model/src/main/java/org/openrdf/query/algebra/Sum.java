/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author Jeen Broekstra
 */
public class Sum extends UnaryValueOperator implements AggregateOperator {

	public Sum(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Sum && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Sum".hashCode();
	}

	@Override
	public Sum clone() {
		return (Sum)super.clone();
	}
}
