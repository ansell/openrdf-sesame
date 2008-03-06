/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.iterator;

import org.openrdf.model.Value;
import org.openrdf.querylogic.EvaluationStrategy;
import org.openrdf.querylogic.QuerySolution;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querymodel.Extension;
import org.openrdf.querymodel.ExtensionElem;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.IteratorWrapper;

public class ExtensionIterator extends IteratorWrapper<Solution> {

	private EvaluationStrategy _strategy;
	private final Extension _extension;
	private TripleSource _tripleSource;

	public ExtensionIterator(EvaluationStrategy strategy, Extension extension, TripleSource tripleSource, Solution bindings) {
		super(strategy.evaluate(extension.getArg(), tripleSource, bindings));
		_strategy = strategy;
		_extension = extension;
		_tripleSource = tripleSource;
	}

	@Override
	public Solution next()
	{
		Solution sourceBindings = super.next();

		QuerySolution targetBindings = new QuerySolution(sourceBindings);

		for (ExtensionElem extElem : _extension.getElements()) {
			Value targetValue = _strategy.getValue(extElem.getExpr(), _tripleSource, sourceBindings);
			targetBindings.addBinding(extElem.getName(), targetValue);
		}

		return targetBindings;
	}
}
