/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author David Huynh
 */
public class Min extends UnaryValueOperator implements AggregateOperator {

	public Min(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public ValueExpr cloneValueExpr() {
		return clone();
	}

	@Override
	public Min clone() {
		return (Min)super.clone();
	}
}
