/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.store.StoreException;

public class JoinCursor implements Cursor<BindingSet> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final TupleExpr rightArg;

	private final Cursor<BindingSet> leftCursor;

	private volatile Cursor<BindingSet> rightCursor;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public JoinCursor(EvaluationStrategy strategy, Cursor<BindingSet> leftCursor, TupleExpr rightArg)
		throws EvaluationException
	{
		this.strategy = strategy;
		this.rightArg = rightArg;
		this.leftCursor = leftCursor;

		// Initialize with empty cursor so that var is never null
		this.rightCursor = EmptyCursor.getInstance();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BindingSet next()
		throws StoreException
	{
		BindingSet rightBindings = rightCursor.next();

		while (rightBindings == null) {
			// right cursor exhausted
			rightCursor.close();

			BindingSet leftBindings = leftCursor.next();
			if (leftBindings != null) {
				rightCursor = strategy.evaluate(rightArg, leftBindings);
				rightBindings = rightCursor.next();
			}
			else {
				// left cursor exhausted
				leftCursor.close();
				break;
			}
		}

		return rightBindings;
	}

	public void close()
		throws StoreException
	{
		leftCursor.close();
		rightCursor.close();
	}

	@Override
	public String toString() {
		String result = "Join\n";
		result += leftCursor.toString();
		result += "\n";

		if (rightCursor instanceof EmptyCursor) {
			result += rightArg.toString();
		}
		else {
			result += rightCursor.toString();
		}

		return result.replace("\n", "\n\t");
	}
}
