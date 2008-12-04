/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultUtil;

/**
 * @author Arjohn Kampman
 * @deprecated
 */
@Deprecated
public class CompatibleBindingSetFilter extends FilterIteration<BindingSet, QueryEvaluationException> {

	private final BindingSet inputBindings;

	public CompatibleBindingSetFilter(CloseableIteration<BindingSet, QueryEvaluationException> iter,
			BindingSet inputBindings)
	{
		super(iter);

		assert inputBindings != null;
		this.inputBindings = inputBindings;
	}

	@Override
	protected boolean accept(BindingSet outputBindings)
		throws QueryEvaluationException
	{
		return QueryResultUtil.bindingSetsCompatible(inputBindings, outputBindings);
	}
}
