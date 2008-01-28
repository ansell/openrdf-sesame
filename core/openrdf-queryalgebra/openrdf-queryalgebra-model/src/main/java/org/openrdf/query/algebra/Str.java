/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The STR function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-str">SPARQL Query Language
 * for RDF</a>; returns the label of literals or the string representation of
 * URIs.
 * 
 * @author Arjohn Kampman
 */
public class Str extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Str() {
	}

	public Str(ValueExpr arg) {
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
	public Str clone() {
		return (Str)super.clone();
	}
}
