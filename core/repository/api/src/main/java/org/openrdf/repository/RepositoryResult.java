/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.CloseableIterationBase;
import info.aduna.iteration.DistinctIteration;
import info.aduna.iteration.Iteration;
import info.aduna.iteration.Iterations;

/**
 * A RepositoryResult is a result collection of objects (for example
 * {@link org.openrdf.model.Statement}, {@link org.openrdf.model.Namespace}, or
 * {@link org.openrdf.model.Resource} objects) that can be iterated over. It
 * keeps an open connection to the backend for lazy retrieval of individual
 * results. Additionally it has some utility methods to fetch all results and
 * add them to a collection.
 * <p>
 * By default, a RepositoryResult is not necessarily a (mathematical) set: it
 * may contain duplicate objects. Duplicate filtering can be 
 * {@link #enableDuplicateFilter() switched on}, but this should not be used
 * lightly as the filtering mechanism is potentially memory-intensive.
 * <p>
 * A RepositoryResult needs to be {@link #close() closed} after use to free up
 * any resources (open connections, read locks, etc.) it has on the underlying
 * repository.
 * 
 * @see RepositoryConnection#getStatements(org.openrdf.model.Resource,
 *      org.openrdf.model.URI, org.openrdf.model.Value, boolean,
 *      org.openrdf.model.Resource[])
 * @see RepositoryConnection#getNamespaces()
 * @see RepositoryConnection#getContextIDs()
 * @author Jeen Broekstra
 * @author Arjohn Kampman
 */
public class RepositoryResult<T> extends CloseableIterationBase<T, RepositoryException> {

	private volatile Iteration<? extends T, RepositoryException> wrappedIter;

	public RepositoryResult(CloseableIteration<? extends T, RepositoryException> iter) {
		assert iter != null;
		wrappedIter = iter;
	}

	public boolean hasNext()
		throws RepositoryException
	{
		return wrappedIter.hasNext();
	}

	public T next()
		throws RepositoryException
	{
		return wrappedIter.next();
	}

	public void remove()
		throws RepositoryException
	{
		wrappedIter.remove();
	}

	@Override
	protected void handleClose()
		throws RepositoryException
	{
		super.handleClose();
		Iterations.closeCloseable(wrappedIter);
	}

	/**
	 * Switches on duplicate filtering while iterating over objects. The
	 * RepositoryResult will keep track of the previously returned objects in a
	 * {@link java.util.Set} and on calling next() or hasNext() will ignore any
	 * objects that already occur in this Set.
	 * <P>
	 * Caution: use of this filtering mechanism is potentially memory-intensive.
	 * 
	 * @throws RepositoryException
	 *         if a problem occurred during initialization of the filter.
	 */
	public void enableDuplicateFilter()
		throws RepositoryException
	{
		if (wrappedIter instanceof DistinctIteration) {
			return;
		}

		wrappedIter = new DistinctIteration<T, RepositoryException>(wrappedIter);
	}
}
