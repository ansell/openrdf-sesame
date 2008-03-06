/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;




/**
 * The DATATYPE function, as defined in
 * <a href="http://www.w3.org/TR/rdf-sparql-query/#func-datatype">SPARQL
 * Query Language for RDF</a>.
 */
public class Datatype extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Datatype(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}
	
	public String toString() {
		return "DATATYPE";
	}
}
