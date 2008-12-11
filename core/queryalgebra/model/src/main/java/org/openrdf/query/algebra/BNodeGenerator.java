/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A BNode generator, which generates a new BNode for each new call to
 * {@link #getValue}.
 */
public class BNodeGenerator extends QueryModelNodeBase implements ValueExpr {

	private static final long serialVersionUID = -5178853853484084984L;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BNodeGenerator() {
		super();
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
	public BNodeGenerator clone() {
		return (BNodeGenerator)super.clone();
	}
}
