/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Projection;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

public class ProjectionIterator extends IterationWrapper<BindingSet, QueryEvaluationException> {

	/**
	 * 
	 */
	private final Projection _projection;

	public ProjectionIterator(CloseableIteration<BindingSet, QueryEvaluationException> eval,
			Projection projection, BindingSet bindings)
	{
		super(eval);
		_projection = projection;
	}

	@Override
	public BindingSet next()
		throws QueryEvaluationException
	{
		return project(_projection, super.next());
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
