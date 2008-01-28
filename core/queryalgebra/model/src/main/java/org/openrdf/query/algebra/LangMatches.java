/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Checks whether a language tag (e.g. "en-US") matches some language range
 * (e.g. "en" or "*").
 */
public class LangMatches extends BinaryValueOperator {

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LangMatches() {
	}

	public LangMatches(ValueExpr leftArg, ValueExpr rightArg) {
		super(leftArg, rightArg);
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
	public LangMatches clone() {
		return (LangMatches)super.clone();
	}
}
