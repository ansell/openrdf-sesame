/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.FilterIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Selection;
import org.openrdf.query.algebra.evaluation.BooleanExprEvaluationException;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

public class SelectionIterator extends FilterIteration<BindingSet, QueryEvaluationException> {

	private EvaluationStrategy _strategy;
	private final Selection _selection;

	public SelectionIterator(EvaluationStrategy strategy, Selection selection, BindingSet bindings) throws QueryEvaluationException {
		super(strategy.evaluate(selection.getArg(), bindings));
		_strategy = strategy;
		_selection = selection;
	}

	@Override
	protected boolean accept(BindingSet bindings) throws QueryEvaluationException
	{
		try {
			return _strategy.isTrue(_selection.getCondition(), bindings);
		}
		catch (BooleanExprEvaluationException e) {
			// failed to evaluate condition
			return false;
		}
	}
}
