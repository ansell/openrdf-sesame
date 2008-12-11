/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
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
 */
public class Bound extends QueryModelNodeBase implements ValueExpr {

	private static final long serialVersionUID = 7222248835654567074L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's argument.
	 */
	protected Var arg;

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
		return arg;
	}

	/**
	 * Sets the argument of this unary value operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(Var arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.arg = arg;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		arg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement)
	{
		if (arg == current) {
			setArg((Var)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public Bound clone() {
		Bound clone = (Bound)super.clone();
		clone.setArg(getArg().clone());
		return clone;
	}
}
