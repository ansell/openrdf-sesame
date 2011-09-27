/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.ConvertingIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Inserts original bindings into the result.
 * 
 * @author Andreas Schwarte
 */
public class InsertBindingsIteration extends ConvertingIteration<BindingSet, BindingSet, QueryEvaluationException>{

	protected final BindingSet bindings;
	
	public InsertBindingsIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter, BindingSet bindings) {
		super(iter);
		this.bindings = bindings;
	}

	@Override
	protected BindingSet convert(BindingSet bIn) throws QueryEvaluationException {
		QueryBindingSet res = new QueryBindingSet(bindings.size() + bIn.size());
		res.addAll(bindings);
		res.addAll(bIn);
		return res;
	}
}
