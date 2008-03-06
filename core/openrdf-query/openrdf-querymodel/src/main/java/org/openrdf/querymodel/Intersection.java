/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * The INTERSECT set operator, which returns the intersection of the result sets
 * of two tuple expressions.
 */
public class Intersection extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new intersection operator that operates on the two specified
	 * arguments.
	 * 
	 * @param leftArg
	 *        The left argument of the intersection operator.
	 * @param rightArg
	 *        The right argument of the intersection operator.
	 */
	public Intersection(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.retainAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		return "INTERSECT";
	}
}
