/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query;

import info.aduna.iteration.CloseableIteration;

/**
 * Super type of all query result types (TupleQueryResult, GraphQueryResult,
 * etc.).
 * 
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public interface QueryResult<T> extends CloseableIteration<T, QueryEvaluationException> {

	/**
	 * Returns a single element from the query result. The QueryResult is
	 * automatically closed by this method.
	 * 
	 * @since 2.7.0
	 * @return a single query result element.
	 * @throws QueryEvaluationException
	 */
	T singleResult()
		throws QueryEvaluationException;

}
