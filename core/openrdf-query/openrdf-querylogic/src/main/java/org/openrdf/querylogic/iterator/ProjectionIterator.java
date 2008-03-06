/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querylogic.iterator;

import org.openrdf.model.Value;
import org.openrdf.querylogic.QuerySolution;
import org.openrdf.querylogic.TripleSource;
import org.openrdf.querymodel.Projection;
import org.openrdf.querymodel.ProjectionElem;
import org.openrdf.queryresult.Solution;
import org.openrdf.util.iterator.CloseableIterator;
import org.openrdf.util.iterator.IteratorWrapper;

public class ProjectionIterator extends IteratorWrapper<Solution> {

	/**
	 * 
	 */
	private final Projection _projection;

	public ProjectionIterator(CloseableIterator<Solution> eval, Projection projection,
			TripleSource tripleSource, Solution bindings)
	{
		super(eval);
		_projection = projection;
	}

	@Override
	public Solution next()
	{
		return project(_projection, super.next());
	}

	public static Solution project(Projection projection, Solution sourceBindings) {
		QuerySolution resultBindings = new QuerySolution(projection.getElements().size());

		for (ProjectionElem pe : projection.getElements()) {
			Value targetValue = sourceBindings.getValue(pe.getSourceName());
			resultBindings.addBinding(pe.getTargetName(), targetValue);
		}

		return resultBindings;
	}
}
