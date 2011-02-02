/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The AVG operator as defined in
 * http://www.w3.org/TR/sparql11-query/#aggregates.
 * <P>
 * Note that we introduce AVG as a first-class object into the algebra,
 * despite it being defined as a compound of other operators (namely, SUM and
 * COUNT). This allows us to more easily optimize evaluation.
 * 
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
