/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The BOUND function, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#func-bound">SPARQL Query
 * Language for RDF</a>; checks if a variable is bound.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class Bound extends UnaryValueOperator implements ValueExpr {

	private static final long serialVersionUID = 7222248835654567074L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Bound() {
	}

	public Bound(Var arg) {
		setArg(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the argument of this unary value operator.
	 * 
	 * @return The operator's argument.
	 */
	public Var getArg() {
		return (Var)super.getArg();
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(ValueExpr arg) {
		assert arg instanceof Var;
		super.setArg(arg);
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
