/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.FilterIteration;

import org.openrdf.StoreException;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.QueryResultUtil;

/**
 * @author Arjohn Kampman
 */
public class CompatibleBindingSetFilter extends FilterIteration<BindingSet, StoreException> {

	private BindingSet inputBindings;

	public CompatibleBindingSetFilter(CloseableIteration<BindingSet, StoreException> iter,
			BindingSet inputBindings)
	{
		super(iter);
		this.inputBindings = inputBindings;
	}

	@Override
	protected boolean accept(BindingSet outputBindings)
		throws EvaluationException
	{
		return QueryResultUtil.bindingSetsCompatible(inputBindings, outputBindings);
	}
}
