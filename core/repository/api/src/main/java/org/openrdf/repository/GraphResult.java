/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.StoreException;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.Cursor;
import org.openrdf.query.base.FilteringCursor;

/**
 * A GraphResult is a result collection of {@link org.openrdf.model.Statement}
 * that can be iterated over. It keeps an open connection to the backend for
 * lazy retrieval of individual results. Additionally it has some utility
 * methods to fetch all results and add them to a collection.
 * <p>
 * By default, a RepositoryResult is not necessarily a (mathematical) set: it
 * may contain duplicate objects. Duplicate filtering can be {
 * {@link #enableDuplicateFilter() switched on}, but this should not be used
 * lightly as the filtering mechanism is potentially memory-intensive.
 * <p>
 * A GraphResult needs to be {@link #close() closed} after use to free up any
 * resources (open connections, read locks, etc.) it has on the underlying
 * repository.
 * 
 * @see RepositoryConnection#getStatements(org.openrdf.model.Resource,
 *      org.openrdf.model.URI, org.openrdf.model.Value, boolean,
 *      org.openrdf.model.Resource[])
 * @author James Leigh
 */
@Deprecated
public class GraphResult extends RepositoryResult<Statement> {

	public GraphResult(Cursor<? extends Statement> cursor) {
		super(cursor);
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
	@Override
	public void enableDuplicateFilter()
		throws StoreException
	{
		delegate = new FilteringCursor<Statement>(delegate) {

			private Set<List<Value>> excludedSet = new HashSet<List<Value>>();

			@Override
			protected boolean accept(Statement st)
				throws StoreException
			{
				Resource s = st.getSubject();
				URI p = st.getPredicate();
				Value o = st.getObject();
				return excludedSet.add(Arrays.asList(new Value[] { s, p, o }));
			}
		};
	}

}
