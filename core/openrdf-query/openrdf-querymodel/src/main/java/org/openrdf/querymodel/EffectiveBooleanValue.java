/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;


/**
 *
 * @author arjohn
 */
public class EffectiveBooleanValue extends ValueTest {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public EffectiveBooleanValue(ValueExpr arg) {
		super(arg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return "EBV";
	}
}
