/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.aduna.iteration.LookAheadIteration;

import org.openrdf.query.Cursor;
import org.openrdf.query.base.FilteringCursor;
import org.openrdf.store.StoreException;

/**
 * A RepositoryResult is a result collection of objects (for example
 * {@link org.openrdf.model.Statement}, {@link org.openrdf.model.Namespace},
 * or {@link org.openrdf.model.Resource} objects) that can be iterated over. It
 * keeps an open connection to the backend for lazy retrieval of individual
 * results. Additionally it has some utility methods to fetch all results and
 * add them to a collection.
 * <p>
 * By default, a RepositoryResult is not necessarily a (mathematical) set: it
 * may contain duplicate objects. Duplicate filtering can be {{@link #enableDuplicateFilter() switched on},
 * but this should not be used lightly as the filtering mechanism is potentially
 * memory-intensive.
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
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class RepositoryResult<T> extends LookAheadIteration<T, StoreException> {
	Cursor<? extends T> delegate;

	public RepositoryResult(Cursor<? extends T> delegate) {
		this.delegate = delegate;
	}

	public String toString() {
		return delegate.toString();
	}

	@Override
	protected T getNextElement()
		throws StoreException
	{
		return delegate.next();
	}

	@Override
	protected void handleClose()
		throws StoreException
	{
		super.handleClose();
		delegate.close();
	}

	/**
	 * Switches on duplicate filtering while iterating over objects. The
	 * RepositoryResult will keep track of the previously returned objects in a
	 * {@link java.util.Set} and on calling next() or hasNext() will ignore any
	 * objects that already occur in this Set.
	 * <P>
	 * Caution: use of this filtering mechanism is potentially memory-intensive.
	 * 
	 * @throws StoreException
	 *         if a problem occurred during initialization of the filter.
	 */
	@Deprecated
	public void enableDuplicateFilter()
		throws StoreException
	{
		delegate = new FilteringCursor<T>(delegate) {
			private Set<T> exclude = new HashSet<T>();

			@Override
			protected boolean accept(T next) {
				return exclude.add(next);
			}
		};
	}

	/**
	 * Returns a {@link List} containing all objects of this RepositoryResult in
	 * order of iteration. The RepositoryResult is fully consumed and
	 * automatically closed by this operation.
	 * <P>
	 * Note: use this method with caution! It pulls the entire RepositoryResult
	 * in memory and as such is potentially very memory-intensive.
	 * 
	 * @return a List containing all objects of this RepositoryResult.
	 * @throws StoreException
	 *         if a problem occurred during retrieval of the results.
	 * @see #addTo(Collection)
	 */
	public List<T> asList()
		throws StoreException
	{
		return addTo(new ArrayList<T>());
	}

	/**
	 * Adds all objects of this RepositoryResult to the supplied collection. The
	 * RepositoryResult is fully consumed and automatically closed by this
	 * operation.
	 * 
	 * @return A reference to the collection that was supplied.
	 * @throws StoreException
	 *         if a problem occurred during retrieval of the results.
	 */
	public <C extends Collection<T>> C addTo(C collection)
		throws StoreException
	{
		try {
			while (hasNext()) {
				collection.add(next());
			}

			return collection;
		}
		finally {
			close();
		}
	}
}
