/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Adds more bindings to each of the results.
 * 
 * @author James Leigh
 */
public class InsertBindingSetCursor extends
		IterationWrapper<BindingSet, QueryEvaluationException> {

	private final BindingSet bindings;

	public InsertBindingSetCursor(
			CloseableIteration<BindingSet, QueryEvaluationException> delegate,
			BindingSet bindings) {
		super(delegate);
		this.bindings = bindings;
	}

	@Override
	public BindingSet next() throws QueryEvaluationException {
		BindingSet next = super.next();
		QueryBindingSet result;
		if (next == null) {
			result = null; // NOPMD
		} else {
			int size = bindings.size() + next.size();
			result = new QueryBindingSet(size);
			result.addAll(bindings);
			for (Binding binding : next) {
				result.setBinding(binding);
			}
		}
		return result;
	}

}
