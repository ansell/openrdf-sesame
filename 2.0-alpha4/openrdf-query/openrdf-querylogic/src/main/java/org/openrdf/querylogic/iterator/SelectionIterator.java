/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.iterator;

import org.openrdf.querylogic.BooleanExprEvaluationException;
import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querymodel.Selection;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.FilterIterator;

public class SelectionIterator extends FilterIterator<Solution> {

	private EvaluationStrategy _strategy;
	private final Selection _selection;
	private TripleSource _tripleSource;

	public SelectionIterator(EvaluationStrategy strategy, Selection selection, TripleSource tripleSource, Solution bindings) {
		super(strategy.evaluate(selection.getArg(), tripleSource, bindings));
		_strategy = strategy;
		_selection = selection;
		_tripleSource = tripleSource;
	}

	@Override
	protected boolean accept(Solution bindings)
	{
		try {
			return _strategy.isTrue(_selection.getCondition(), _tripleSource, bindings);
		}
		catch (BooleanExprEvaluationException e) {
			// failed to evaluate condition
			return false;
		}
	}
}
