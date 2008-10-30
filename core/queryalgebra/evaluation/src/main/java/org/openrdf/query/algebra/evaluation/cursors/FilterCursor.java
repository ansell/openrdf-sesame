/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.cursors;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Cursor;
import org.openrdf.query.EvaluationException;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.base.FilteringCursor;
import org.openrdf.store.StoreException;

public class FilterCursor extends FilteringCursor<BindingSet> {

	/*-----------*
	 * Constants *
	 *-----------*/

	private final Filter filter;

	private final EvaluationStrategy strategy;

	/**
	 * The set of binding names that are "in scope" for the filter. The filter
	 * must not include bindings that are (only) included because of the
	 * depth-first evaluation strategy in the evaluation of the constraint.
	 */
	private final Set<String> scopeBindingNames;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public FilterCursor(Filter filter, Cursor<BindingSet> iter,
			EvaluationStrategy strategy)
		throws EvaluationException
	{
		super(iter);
		this.filter = filter;
		this.strategy = strategy;
		this.scopeBindingNames = filter.getBindingNames();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	protected boolean accept(BindingSet bindings)
		throws StoreException
	{
		try {
			// Limit the bindings to the ones that are in scope for this filter
			QueryBindingSet scopeBindings = new QueryBindingSet(bindings);
			scopeBindings.retainAll(scopeBindingNames);

			return strategy.isTrue(filter.getCondition(), scopeBindings);
		}
		catch (ValueExprEvaluationException e) {
			// failed to evaluate condition
			return false;
		}
	}

	@Override
	public String getName() {
		return filter.getCondition().toString();
	}
}
