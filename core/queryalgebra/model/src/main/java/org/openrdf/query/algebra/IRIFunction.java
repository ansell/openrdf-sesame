/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The IRI function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#SparqlOps">SPARQL 1.1 Query
 * Language for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class IRIFunction extends UnaryValueOperator {

	private String baseURI;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public IRIFunction() {
	}

	public IRIFunction(ValueExpr arg) {
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
		return other instanceof IRIFunction && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "IRI".hashCode();
	}

	@Override
	public IRIFunction clone() {
		return (IRIFunction)super.clone();
	}

	/**
	 * @param baseURI The baseURI to set.
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * @return Returns the baseURI.
	 */
	public String getBaseURI() {
		return baseURI;
	}
}
