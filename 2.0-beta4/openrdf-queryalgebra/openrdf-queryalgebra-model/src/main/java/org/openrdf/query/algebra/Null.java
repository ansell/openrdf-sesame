/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The NULL value.
 */
public class Null extends QueryModelNodeBase implements ValueExpr {

	public Null() {
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "null";
	}

	public ValueExpr cloneValueExpr() {
		return new Null();
	}
}
