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
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

public class ProjectionIterator extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException> {

	private final Projection _projection;

	public ProjectionIterator(EvaluationStrategy strategy, Projection projection, BindingSet bindings)
		throws QueryEvaluationException
	{
		super(strategy.evaluate(projection.getArg(), bindings));
		_projection = projection;
	}

	public BindingSet convert(BindingSet sourceBindings)
		throws QueryEvaluationException
	{
		return project(_projection, sourceBindings);
	}

	public static BindingSet project(Projection projection, BindingSet sourceBindings) {
		QueryBindingSet resultBindings = new QueryBindingSet(projection.getElements().size());

		for (ProjectionElem pe : projection.getElements()) {
			Value targetValue = sourceBindings.getValue(pe.getSourceName());
			if (targetValue != null) {
				resultBindings.addBinding(pe.getTargetName(), targetValue);
			}
		}

		return resultBindings;
	}
}
