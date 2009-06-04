/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.result.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.cursor.Cursor;
import org.openrdf.cursor.DelegatingCursor;
import org.openrdf.result.MultipleResultException;
import org.openrdf.result.NoResultException;
import org.openrdf.result.Result;
import org.openrdf.store.StoreException;

/**
 * A RepositoryResult is a result collection of objects (for example
 * {@link org.openrdf.model.Statement}, {@link org.openrdf.model.Namespace}, or
 * {@link org.openrdf.model.Resource} objects) that can be iterated over. It
 * keeps an open connection to the backend for lazy retrieval of individual
 * results. Additionally it has some utility methods to fetch all results and
 * add them to a collection.
 * <p>
 * A RepositoryResult needs to be {@link #close() closed} after use to free up
 * any resources (open connections, read locks, etc.) it has on the underlying
 * repository.
 * 
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class ResultImpl<T> extends DelegatingCursor<T> implements Result<T> {

	private T next;

	public ResultImpl(Cursor<? extends T> delegate) {
		super(delegate);
	}

	public boolean hasNext()
		throws StoreException
	{
		return next != null || (next = next()) != null;
	}

	@Override
	public T next()
		throws StoreException
	{
		T result = next;
		if (result == null) {
			return super.next();
		}
		next = null;
		return result;
	}

	/**
	 * Returns the value of this RepositoryResult. The RepositoryResult is fully
	 * consumed and automatically closed by this operation.
	 * 
	 * @return the only object of this RepositoryResult.
	 * @throws StoreException
	 *         if a problem occurred during retrieval of the results.
	 * @throws NonUniqueResultException
	 *         if the result did not contain exactly one result.
	 * @see #addTo(Collection)
	 */
	public T singleResult()
		throws StoreException
	{
		try {
			T next = next();
			if (next == null) {
				throw new NoResultException("No result");
			}
			if (next() != null) {
				throw new MultipleResultException("More than one result");
			}
			return next;
		}
		finally {
			close();
		}
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
	 * Returns a {@link Set} containing all objects of this RepositoryResult. The
	 * RepositoryResult is fully consumed and automatically closed by this
	 * operation.
	 * <P>
	 * Note: use this method with caution! It pulls the entire RepositoryResult
	 * in memory and as such is potentially very memory-intensive.
	 * 
	 * @return a Set containing all objects of this RepositoryResult.
	 * @throws StoreException
	 *         if a problem occurred during retrieval of the results.
	 * @see #addTo(Collection)
	 */
	public Set<T> asSet()
		throws StoreException
	{
		return addTo(new LinkedHashSet<T>());
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
	public <C extends Collection<? super T>> C addTo(C collection)
		throws StoreException
	{
		try {
			T next;
			while ((next = next()) != null) {
				collection.add(next);
			}

			return collection;
		}
		finally {
			close();
		}
	}
}
