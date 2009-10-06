/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The DATATYPE function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-datatype">SPARQL Query
 * Language for RDF</a>.
 * 
 * @author Arjohn Kampman
 */
public class Datatype extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Datatype() {
	}

	public Datatype(ValueExpr arg) {
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
		return other instanceof Datatype && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Datatype".hashCode();
	}

	@Override
	public Datatype clone() {
		return (Datatype)super.clone();
	}
}
