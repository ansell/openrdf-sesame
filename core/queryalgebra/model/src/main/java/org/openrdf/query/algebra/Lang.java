/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LANG function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-lang">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Arjohn Kampman
 */
public class Lang extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Lang() {
	}

	public Lang(ValueExpr arg) {
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
		return other instanceof Lang && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Lang".hashCode();
	}

	@Override
	public Lang clone() {
		return (Lang)super.clone();
	}
}
