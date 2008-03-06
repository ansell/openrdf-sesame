/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;



/**
 * A BNode generator, which generates a new BNode for each new call to
 * {@link #getValue}.
 */
public class BNodeGenerator extends ValueExpr {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BNodeGenerator() {
		super();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return "BNODE_GENERATOR";
	}
}
