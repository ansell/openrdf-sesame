/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The UCASE function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-ucase">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class UpperCase extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public UpperCase() {
	}

	public UpperCase(ValueExpr arg) {
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
		return other instanceof UpperCase && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "UCASE".hashCode();
	}

	@Override
	public UpperCase clone() {
		return (UpperCase)super.clone();
	}
}
