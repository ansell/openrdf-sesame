/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The StrEnds function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strends">SPARQL 1.1 Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class StrEnds extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StrEnds() {
	}

	public StrEnds(ValueExpr leftArg, ValueExpr rightArg) {
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
		return other instanceof StrEnds && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "STRENDS".hashCode();
	}

	@Override
	public StrEnds clone() {
		return (StrEnds)super.clone();
	}
}
