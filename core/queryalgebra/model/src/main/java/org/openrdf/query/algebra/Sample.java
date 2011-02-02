/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The SAMPLE operator as defined in http://www.w3.org/TR/sparql11-query/#aggregates
 * 
 * @author Jeen Broekstra
 */
public class Sample extends UnaryValueOperator implements AggregateOperator {

	public Sample(ValueExpr arg) {
		super(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Sample && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Sample".hashCode();
	}

	@Override
	public Sample clone() {
		return (Sample)super.clone();
	}
}
