/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.QueryResultUtil;
import org.openrdf.query.base.FilteringCursor;

/**
 * @author Arjohn Kampman
 * @deprecated
 */
@Deprecated
public class CompatibleBindingSetFilter extends FilteringCursor<BindingSet> {

	private BindingSet inputBindings;

	public CompatibleBindingSetFilter(Cursor<BindingSet> iter, BindingSet inputBindings) {
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
