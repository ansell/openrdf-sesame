/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The DATATYPE function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-datatype">SPARQL Query
 * Language for RDF</a>.
 */
public class DatatypeFunc extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public DatatypeFunc() {
	}

	public DatatypeFunc(ValueExpr arg) {
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
		return "DATATYPE";
	}

	public ValueExpr cloneValueExpr() {
		return new DatatypeFunc(getArg().cloneValueExpr());
	}
}
