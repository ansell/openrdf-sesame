/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import java.util.Iterator;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;

/**
 * Iteration which forms the cross product of a list of materialized input bindings
 * with each result obtained from the inner iteration. See {@link SPARQLFederatedService}.
 * 
 * Example:
 * <source>
 * inputBindings := {b1, b2, ...}
 * resultIteration := {r1, r2, ...}
 * 
 * getNextElement() returns (r1,b1), (r1, b2), ..., (r2, b1), (r2, b2), ...
 * 
 * i.e. compute the cross product per result binding (see {@link #computedCrossProduct}
 * </source>
 * 
 * 
 * @author Andreas Schwarte
 */
public class ServiceCrossProductIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {

	protected final List<BindingSet> inputBindings;
	protected final CloseableIteration<BindingSet, QueryEvaluationException> resultIteration;

	protected Iterator<BindingSet> inputBindingsIterator = null;
	protected BindingSet currentInputBinding = null;
	
	public ServiceCrossProductIteration(
			CloseableIteration<BindingSet, QueryEvaluationException> resultIteration,
			List<BindingSet> inputBindings) {
		super();
		this.resultIteration = resultIteration;
		this.inputBindings = inputBindings;
	}
	
	@Override
	protected BindingSet getNextElement() throws QueryEvaluationException {
		
		if (currentInputBinding==null) {
			inputBindingsIterator = inputBindings.iterator();
			if (resultIteration.hasNext())
				currentInputBinding = resultIteration.next();
			else
				return null;  // no more results
		}	
		
		if (inputBindingsIterator.hasNext()) {
			BindingSet next = inputBindingsIterator.next();
			QueryBindingSet res = new QueryBindingSet(next.size() + currentInputBinding.size() );
			res.addAll(next);
			res.addAll(currentInputBinding);
			if (!inputBindingsIterator.hasNext())
				currentInputBinding = null;
			return res;
		}
		
		return null;
	}	
}
