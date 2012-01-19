/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.QueryEvaluationException;

/**
 * Result consumer for handing over results from the executor to the consuming
 * iteration
 * 
 * @author Andreas Schwarte
 * @see ServiceJoinIterator
 */
public interface ResultConsumer<T> {
	
	/**
	 * Add the specified computed result to the consuming iteration
	 * @param res
	 */
	public void addResult(CloseableIteration<T, QueryEvaluationException> res);
}
