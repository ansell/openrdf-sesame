/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A natural join between two tuple expressions.
 */
public class Join extends BinaryTupleOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Join() {
	}

	/**
	 * Creates a new natural join operator.
	 */
	public Join(TupleExpr leftArg, TupleExpr rightArg) {
		super(leftArg, rightArg);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public Set<String> getBindingNames() {
		Set<String> bindingNames = new LinkedHashSet<String>(16);
		bindingNames.addAll(getLeftArg().getBindingNames());
		bindingNames.addAll(getRightArg().getBindingNames());
		return bindingNames;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public String toString() {
		return "JOIN";
	}

	public TupleExpr cloneTupleExpr() {
		TupleExpr leftArg = getLeftArg().cloneTupleExpr();
		TupleExpr rightArg = getRightArg().cloneTupleExpr();
		return new Join(leftArg, rightArg);
	}
}
