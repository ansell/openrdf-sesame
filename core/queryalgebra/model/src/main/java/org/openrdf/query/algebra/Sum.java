/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The SUM operator as defined in http://www.w3.org/TR/sparql11-query/#aggregates
 * 
 * @author Jeen Broekstra
 */
public class Sum extends AggregateOperatorBase {

	public Sum(ValueExpr arg) {
		super(arg);
	}
	
	public Sum(ValueExpr arg, boolean distinct) {
		super(arg, distinct);
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
