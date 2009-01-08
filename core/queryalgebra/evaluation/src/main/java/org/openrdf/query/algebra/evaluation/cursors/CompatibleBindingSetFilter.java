/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.FilteringCursor;
import org.openrdf.query.BindingSet;
import org.openrdf.query.EvaluationException;
import org.openrdf.result.util.QueryResultUtil;

/**
 * @author Arjohn Kampman
 * @deprecated
 */
@Deprecated
public class CompatibleBindingSetFilter extends FilteringCursor<BindingSet> {

	private final BindingSet inputBindings;

	public CompatibleBindingSetFilter(Cursor<BindingSet> iter, BindingSet inputBindings) {
		super(iter);
		assert inputBindings != null;
		this.inputBindings = inputBindings;
	}

	@Override
	protected boolean accept(BindingSet outputBindings)
		throws EvaluationException
	{
		return QueryResultUtil.bindingSetsCompatible(inputBindings, outputBindings);
	}
}
