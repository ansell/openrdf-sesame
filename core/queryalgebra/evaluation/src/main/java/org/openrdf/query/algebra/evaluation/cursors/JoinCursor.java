/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.results.Cursor;
import org.openrdf.store.StoreException;

public class JoinCursor implements Cursor<BindingSet> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final TupleExpr rightArg;

	/*-----------*
	 * Variables *
	 *-----------*/

	private Cursor<BindingSet> leftIter;

	private Cursor<BindingSet> rightIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public JoinCursor(EvaluationStrategy strategy,
			Cursor<BindingSet> leftIter, TupleExpr rightArg,
			BindingSet bindings)
		throws EvaluationException
	{
		this.strategy = strategy;
		this.leftIter = leftIter;
		this.rightArg = rightArg;
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BindingSet next()
		throws StoreException
	{
		BindingSet leftNext = null;
		while (rightIter != null || (leftNext = leftIter.next()) != null) {
			if (rightIter == null) {
				rightIter = strategy.evaluate(rightArg, leftNext);
			}

			BindingSet rightNext = rightIter.next();
			if (rightNext != null) {
				return rightNext;
			}
			else {
				rightIter.close();
				rightIter = null;
			}
		}

		return null;
	}

	public void close()
		throws StoreException
	{
		if (rightIter != null) {
			rightIter.close();
			rightIter = null;
		}

		leftIter.close();
	}

	@Override
	public String toString() {
		String left = leftIter.toString().replace("\n", "\n\t");
		String right = rightArg.toString();
		if (rightIter != null) {
			right = rightIter.toString();
		}
		return "Join\n\t" + left + "\n\t" + right.replace("\n", "\n\t");
	}
}
