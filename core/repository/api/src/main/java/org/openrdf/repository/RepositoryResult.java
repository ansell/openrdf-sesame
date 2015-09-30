/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.openrdf.repository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.AbstractCloseableIteration;
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
public class RepositoryResult<T> extends AbstractCloseableIteration<T, RepositoryException> {

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

	/**
	 * Returns a {@link List} containing all objects of this RepositoryResult in
	 * order of iteration. The RepositoryResult is fully consumed and
	 * automatically closed by this operation.
	 * <P>
	 * Note: use this method with caution! It pulls the entire RepositoryResult
	 * in memory and as such is potentially very memory-intensive.
	 * 
	 * @return a List containing all objects of this RepositoryResult.
	 * @throws RepositoryException
	 *         if a problem occurred during retrieval of the results.
	 * @see #addTo(Collection)
	 * @deprecated Use {@link Iterations#asList(Iteration)} instead.
	 */
	@Deprecated
	public List<T> asList()
		throws RepositoryException
	{
		return addTo(new ArrayList<T>());
	}

	/**
	 * Adds all objects of this RepositoryResult to the supplied collection. The
	 * RepositoryResult is fully consumed and automatically closed by this
	 * operation.
	 * 
	 * @return A reference to the collection that was supplied.
	 * @throws RepositoryException
	 *         if a problem occurred during retrieval of the results.
	 * @deprecated Use {@link Iterations#addAll(Iteration, Collection)} instead.
	 */
	@Deprecated
	public <C extends Collection<T>> C addTo(C collection)
		throws RepositoryException
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
