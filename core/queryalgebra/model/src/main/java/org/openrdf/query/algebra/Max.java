/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author David Huynh
 */
public class Max extends AggregateOperatorBase {

	public Max(ValueExpr arg) {
		super(arg);
	}
	
	public Max(ValueExpr arg, boolean distinct) {
		super(arg, distinct);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Max && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Max".hashCode();
	}

	@Override
	public Max clone() {
		return (Max)super.clone();
	}
}
