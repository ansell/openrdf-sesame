/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The NAMESPACE function, which selects the namespace of URIs.
 * 
 * @author Arjohn Kampman
 */
public class Namespace extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Namespace() {
	}

	public Namespace(ValueExpr arg) {
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

	public Namespace clone() {
		return (Namespace)super.clone();
	}
}
