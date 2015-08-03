/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package info.aduna.iteration;

import java.util.NoSuchElementException;

/**
 * An iteration that delays the creation of the underlying iteration until it is
 * being accessed. This is mainly useful for situations where iteration creation
 * adds considerable overhead but where the iteration may not actually be used,
 * or where a created iteration consumes scarce resources like JDBC-connections
 * or memory. Subclasses must implement the <tt>createIteration</tt> method,
 * which is called once when the iteration is first needed.
 */
public abstract class DelayedIteration<E, X extends Exception> extends AbstractCloseableIteration<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Iteration<? extends E, ? extends X> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new DelayedIteration.
	 */
	public DelayedIteration() {
		super();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Creates the iteration that should be iterated over. This method is called
	 * only once, when the iteration is first needed.
	 */
	protected abstract Iteration<? extends E, ? extends X> createIteration()
		throws X;

	/**
	 * Calls the <tt>hasNext</tt> method of the underlying iteration.
	 */
	public boolean hasNext()
		throws X
	{
		if (iter == null) {
			// Underlying iterator has not yet been initialized
			synchronized (this) {
				if (isClosed()) {
					return false;
				}
				else {
					iter = createIteration();
				}
			}
		}

		return iter.hasNext();
	}

	/**
	 * Calls the <tt>next</tt> method of the underlying iteration.
	 */
	public E next()
		throws X
	{
		if (iter == null) {
			// Underlying iterator has not yet been initialized
			synchronized (this) {
				if (isClosed()) {
					throw new NoSuchElementException("Iteration has been closed");
				}
				else {
					iter = createIteration();
				}
			}
		}

		return iter.next();
	}

	/**
	 * Calls the <tt>remove</tt> method of the underlying iteration.
	 */
	public void remove()
		throws X
	{
		if (iter == null || isClosed()) {
			throw new IllegalStateException();
		}

		iter.remove();
	}

	/**
	 * Closes this iteration as well as the underlying iteration if it has
	 * already been created and happens to be a {@link CloseableIteration}.
	 */
	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();

		synchronized (this) {
			Iterations.closeCloseable(iter);
		}
	}
}
