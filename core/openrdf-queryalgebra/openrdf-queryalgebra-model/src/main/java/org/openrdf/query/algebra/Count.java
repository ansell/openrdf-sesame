/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author David Huynh
 */
public class Count extends UnaryValueOperator implements AggregateOperator {

	public Count() {
		// FIXME: should we create a separate class for this case?
		super(null);
	}

	public Count(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String toString() {
		return "COUNT";
	}

	public ValueExpr cloneValueExpr() {
		if (_arg == null)
			return new Count();
		return new Count(getArg().cloneValueExpr());
	}
}
