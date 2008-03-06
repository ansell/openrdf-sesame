/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LANG function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-lang">SPARQL Query Language
 * for RDF</a>.
 */
public class LangFunc extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LangFunc() {
	}

	public LangFunc(ValueExpr arg) {
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

	public String toString() {
		return "lang";
	}

	public ValueExpr cloneValueExpr() {
		return new LangFunc(getArg().cloneValueExpr());
	}
}
