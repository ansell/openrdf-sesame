/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author Jeen Broekstra
 */
public class Avg extends UnaryValueOperator implements AggregateOperator {

	public Avg(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Avg && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Avg".hashCode();
	}

	@Override
	public Avg clone() {
		return (Avg)super.clone();
	}
}
