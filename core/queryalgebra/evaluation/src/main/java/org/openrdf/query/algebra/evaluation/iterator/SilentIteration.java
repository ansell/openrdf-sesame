/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Wrap an inner iteration and suppress exceptions silently
 * 
 * @author Andreas Schwarte
 */
public class SilentIteration extends LookAheadIteration<BindingSet, QueryEvaluationException> {
	
	protected CloseableIteration<BindingSet, QueryEvaluationException> iter;
	
	public SilentIteration(CloseableIteration<BindingSet, QueryEvaluationException> iter) {
		super();
		this.iter = iter;
	}
	
	
	@Override
	protected BindingSet getNextElement() throws QueryEvaluationException {
		
		try {
			if (iter.hasNext())
				return iter.next();
		} catch (Exception e) {
			// suppress
		}
		
		return null;
	}

}
