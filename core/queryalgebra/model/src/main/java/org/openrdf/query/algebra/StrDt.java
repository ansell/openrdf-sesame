/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The StrDt function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#SparqlOps">SPARQL 1.1 Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class StrDt extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StrDt() {
	}

	public StrDt(ValueExpr leftArg, ValueExpr rightArg) {
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
		return other instanceof StrDt && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "StrDt".hashCode();
	}

	@Override
	public StrDt clone() {
		return (StrDt)super.clone();
	}
}
