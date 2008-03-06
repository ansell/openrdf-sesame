/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.ConvertingIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

public class ExtensionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	private final EvaluationStrategy _strategy;

	private final Extension _extension;

	public ExtensionIterator(EvaluationStrategy strategy, Extension extension, BindingSet bindings)
		throws QueryEvaluationException
	{
		super(strategy.evaluate(extension.getArg(), bindings));
		_strategy = strategy;
		_extension = extension;
	}

	public BindingSet convert(BindingSet sourceBindings)
		throws QueryEvaluationException
	{
		QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);

		for (ExtensionElem extElem : _extension.getElements()) {
			Value targetValue = _strategy.getValue(extElem.getArg(), sourceBindings);

			if (targetValue != null) {
				targetBindings.addBinding(extElem.getName(), targetValue);
			}
		}

		return targetBindings;
	}
}
