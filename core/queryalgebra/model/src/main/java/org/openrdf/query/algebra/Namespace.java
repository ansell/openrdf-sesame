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

	private static final long serialVersionUID = -4488015257656828790L;

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

	@Override
	public Namespace clone() {
		return (Namespace)super.clone();
	}
}
