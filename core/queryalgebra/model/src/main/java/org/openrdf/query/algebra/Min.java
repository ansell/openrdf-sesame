/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * @author David Huynh
 */
public class Min extends  AggregateOperatorBase {

	public Min(ValueExpr arg) {
		super(arg);
	}
	
	public Min(ValueExpr arg, boolean distinct) {
		super(arg, distinct);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Min && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Min".hashCode();
	}

	@Override
	public Min clone() {
		return (Min)super.clone();
	}
}
