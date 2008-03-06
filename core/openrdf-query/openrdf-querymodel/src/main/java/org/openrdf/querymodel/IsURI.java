/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;




public class IsURI extends ValueTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public IsURI(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}
	
	public String toString() {
		return "isURI";
	}
}
