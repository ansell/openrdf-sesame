/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelayedCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class DelayedEvaluationCursor extends DelayedCursor<BindingSet> {

	private final EvaluationStrategy strategy;

	private final TupleExpr expr;

	private final BindingSet bindings;

	public DelayedEvaluationCursor(EvaluationStrategy strategy, TupleExpr expr, BindingSet bindings) {
		this.strategy = strategy;
		this.expr = expr;
		this.bindings = bindings;
	}

	@Override
	protected Cursor<BindingSet> createCursor()
		throws StoreException
	{
		return strategy.evaluate(expr, bindings);
	}

	@Override
	public String toString() {
		if (cursorCreated()) {
			return super.toString();
		}
		else {
			return expr.toString();
		}
	}
}
