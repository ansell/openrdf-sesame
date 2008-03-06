/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

public class IsBNode extends UnaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsBNode() {
	}

	public IsBNode(ValueExpr arg) {
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

	public IsBNode clone() {
		return (IsBNode)super.clone();
	}
}
