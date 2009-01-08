/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.EmptyCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.store.StoreException;

public class LeftJoinCursor implements Cursor<BindingSet> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private final EvaluationStrategy strategy;

	private final LeftJoin leftJoin;

	/**
	 * The set of binding names that are "in scope" for the filter. The filter
	 * must not include bindings that are (only) included because of the
	 * depth-first evaluation strategy in the evaluation of the constraint.
	 */
	private final Set<String> scopeBindingNames;

	private final Cursor<BindingSet> leftCursor;

	private volatile Cursor<BindingSet> rightCursor;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public LeftJoinCursor(EvaluationStrategy strategy, LeftJoin leftJoin, BindingSet bindings)
		throws StoreException
	{
		this.strategy = strategy;
		this.leftJoin = leftJoin;
		this.scopeBindingNames = leftJoin.getBindingNames();

		this.leftCursor = strategy.evaluate(leftJoin.getLeftArg(), bindings);

		// Initialize with empty cursor so that var is never null
		this.rightCursor = EmptyCursor.getInstance();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public BindingSet next()
		throws StoreException
	{
		BindingSet leftBindings = null;
		BindingSet rightBindings = rightCursor.next();

		while (rightBindings != null || (leftBindings = leftCursor.next()) != null) {
			if (rightBindings == null) {
				// right cursor exhausted
				rightCursor.close();

				// join with next value from left argument, use left arg's bindings
				// in case the join fails
				rightCursor = strategy.evaluate(leftJoin.getRightArg(), leftBindings);
				rightBindings = rightCursor.next();
			}

			if (leftJoin.hasCondition()) {
				// Find a binding for which the join condition holds
				while (rightBindings != null) {
					// Limit the bindings to the ones that are in scope for this
					// filter
					QueryBindingSet scopeBindings = new QueryBindingSet(rightBindings);
					scopeBindings.retainAll(scopeBindingNames);

					try {
						if (strategy.isTrue(leftJoin.getCondition(), scopeBindings)) {
							return rightBindings;
						}
					}
					catch (ValueExprEvaluationException e) {
						// Ignore, condition not evaluated successfully
					}

					rightBindings = rightCursor.next();
				}
			}
			else if (rightBindings != null) {
				// Found bindings and no join condition specified
				return rightBindings;
			}

			if (leftBindings != null) {
				// Join failed, return left arg's bindings
				return leftBindings;
			}
		}

		return null;
	}

	public void close()
		throws StoreException
	{
		leftCursor.close();
		rightCursor.close();
	}

	@Override
	public String toString() {
		String result = "LeftJoin ";

		if (leftJoin.hasCondition()) {
			result += leftJoin.getCondition().toString().trim();
		}

		result += "\n";
		result += leftCursor.toString();

		result += "\n";
		if (rightCursor instanceof EmptyCursor) {
			result += leftJoin.getRightArg().toString();
		}
		else {
			result += rightCursor.toString();
		}

		return result.replace("\n", "\n\t");
	}
}
