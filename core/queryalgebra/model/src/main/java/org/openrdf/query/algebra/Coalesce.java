/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;


/**
 *
 * @author Jeen
 */
public class Coalesce extends NAryValueOperator {

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
