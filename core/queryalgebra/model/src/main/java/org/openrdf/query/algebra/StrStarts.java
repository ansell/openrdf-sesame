/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The StrStarts function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strstarts">SPARQL 1.1 Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class StrStarts extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StrStarts() {
	}

	public StrStarts(ValueExpr leftArg, ValueExpr rightArg) {
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
		return other instanceof StrStarts && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "STRSTARTS".hashCode();
	}

	@Override
	public StrStarts clone() {
		return (StrStarts)super.clone();
	}
}
