/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LCASE function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-lcase">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class LowerCase extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LowerCase() {
	}

	public LowerCase(ValueExpr arg) {
		super(arg);
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
		return other instanceof LowerCase && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "LCASE".hashCode();
	}

	@Override
	public LowerCase clone() {
		return (LowerCase)super.clone();
	}
}
