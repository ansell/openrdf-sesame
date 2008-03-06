/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

public class ExtensionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	private final Extension extension;

	private final EvaluationStrategy strategy;

	public ExtensionIterator(Extension extension,
			CloseableIteration<BindingSet, QueryEvaluationException> iter, EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(iter);
		this.extension = extension;
		this.strategy = strategy;
	}

	public BindingSet convert(BindingSet sourceBindings)
		throws QueryEvaluationException
	{
		QueryBindingSet targetBindings = new QueryBindingSet(sourceBindings);

		for (ExtensionElem extElem : extension.getElements()) {
			Value targetValue = strategy.evaluate(extElem.getExpr(), sourceBindings);

			if (targetValue != null) {
				// Potentially overwrites bindings from super
				targetBindings.setBinding(extElem.getName(), targetValue);
			}
		}

		return targetBindings;
	}
}
