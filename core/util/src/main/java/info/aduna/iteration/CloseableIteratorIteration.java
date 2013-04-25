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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iteration that can convert an {@link Iterator} to a
 * {@link CloseableIteration}.
 */
public class CloseableIteratorIteration<E, X extends Exception> extends CloseableIterationBase<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private Iterator<? extends E> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates an uninitialized CloseableIteratorIteration, needs to be
	 * initialized by calling {@link #setIterator(Iterator)} before it can be
	 * used.
	 */
	public CloseableIteratorIteration() {
	}

	/**
	 * Creates a CloseableIteratorIteration that wraps the supplied iterator.
	 */
	public CloseableIteratorIteration(Iterator<? extends E> iter) {
		setIterator(iter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	protected void setIterator(Iterator<? extends E> iter) {
		this.iter = iter;
	}

	public boolean hasNext()
		throws X
	{
		return !isClosed() && iter.hasNext();
	}

	public E next()
		throws X
	{
		if (isClosed()) {
			throw new NoSuchElementException("Iteration has been closed");
		}

		return iter.next();
	}

	public void remove()
		throws X
	{
		if (isClosed()) {
			throw new IllegalStateException("Iteration has been closed");
		}

		iter.remove();
	}
}
