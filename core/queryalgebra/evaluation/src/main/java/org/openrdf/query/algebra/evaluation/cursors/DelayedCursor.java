/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.result.Cursor;
import org.openrdf.store.StoreException;


/**
 *
 * @author James Leigh
 */
public class DelayedCursor implements Cursor<BindingSet> {

	private EvaluationStrategy strategy;

	private TupleExpr expr;

	private BindingSet bindings;

	private Cursor<BindingSet> delegate;

	public DelayedCursor(EvaluationStrategy strategy, TupleExpr expr, BindingSet bindings) {
		this.strategy = strategy;
		this.expr = expr;
		this.bindings = bindings;
	}

	public void close()
		throws StoreException
	{
		if (delegate != null) {
			delegate.close();
		}
	}

	public BindingSet next()
		throws StoreException
	{
		if (delegate == null) {
			delegate = createCursor();
		}
		return delegate.next();
	}

	@Override
	public String toString() {
		if (delegate == null)
			return expr.toString();
		return delegate.toString();
	}

	protected Cursor<BindingSet> createCursor() throws StoreException {
		return strategy.evaluate(expr, bindings);
	}
}
