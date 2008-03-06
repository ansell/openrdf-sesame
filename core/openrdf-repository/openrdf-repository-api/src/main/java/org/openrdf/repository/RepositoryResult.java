/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.util.ArrayList;
import java.util.Collection;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.IterationWrapper;

/**
 * A RepositoryResult is a result set of statements that can be iterated over. It
 * keeps an open connection to the backend for lazy retrieval of individual
 * results. Additionally it can fully consume all statements in the result and
 * return a {@link java.util.Collection} object containing all Statements.
 * <p>
 * A RepositoryResult needs to be closed after use to free up any resources (open
 * connections, read locks, etc.) it has on the underlying repository.
 * 
 * @see CloseableIteration
 * @author jeen
 */
public class RepositoryResult<T> extends IterationWrapper<T, RepositoryException> {

	public RepositoryResult(CloseableIteration<? extends T, RepositoryException> iter) {
		super(iter);
	}

	/**
	 * Creates a {@link java.util.Collection} containing all statements in this
	 * RepositoryResult. The RepositoryResult is fully consumed and automatically
	 * closed by this operation.
	 * <P>
	 * Note: use this method with caution! It pulls  the entire RepositoryResult in memory and
	 * as such is potentially very memory-intensive.
	 * 
	 * @return a Collection object containing all Statements
	 * @throws RepositoryException
	 *         if a problem occurred during retrieval of the results.
	 */
	public Collection<T> asCollection()
		throws RepositoryException
	{
		Collection<T> result = new ArrayList<T>();
		try {
			while (hasNext()) {
				result.add(next());
			}
		}
		finally {
			close();
		}
		return result;
	}
}
