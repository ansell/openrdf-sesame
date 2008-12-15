/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.openrdf.results.Result;
import org.openrdf.store.StoreException;

/**
 * A RepositoryResult is a result collection of objects (for example
 * {@link org.openrdf.model.Statement}, {@link org.openrdf.model.Namespace}, or
 * {@link org.openrdf.model.Resource} objects) that can be iterated over. It
 * keeps an open connection to the backend for lazy retrieval of individual
 * results. Additionally it has some utility methods to fetch all results and
 * add them to a collection.
 * <p>
 * By default, a RepositoryResult is not necessarily a (mathematical) set: it
 * may contain duplicate objects. Duplicate filtering can be {
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
 * @author jeen
 * @author Arjohn Kampman
 * @author James Leigh
 */
@Deprecated
public interface RepositoryResult<T> extends Result<T> {

	public boolean hasNext()
		throws StoreException;

	public List<T> asList()
		throws StoreException;

	public Set<T> asSet()
		throws StoreException;

	public <C extends Collection<? super T>> C addTo(C bindingSets)
		throws StoreException;
}
