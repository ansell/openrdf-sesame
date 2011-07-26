/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The STRLEN function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-strlen">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class StrLen extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public StrLen() {
	}

	public StrLen(ValueExpr arg) {
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
		return other instanceof StrLen && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "STRLEN".hashCode();
	}

	@Override
	public StrLen clone() {
		return (StrLen)super.clone();
	}
}
