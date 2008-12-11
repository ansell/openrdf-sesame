/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The LOCAL NAME function, which selects the local name of URIs.
 * 
 * @author Arjohn Kampman
 */
public class LocalName extends UnaryValueOperator {

	private static final long serialVersionUID = 343832787278986470L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LocalName() {
	}

	public LocalName(ValueExpr arg) {
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
	public LocalName clone() {
		return (LocalName)super.clone();
	}
}
