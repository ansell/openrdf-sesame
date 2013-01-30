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

/**
 * Abstract superclass for Iterations that wrap other Iterations. The abstract
 * class <tt>IterationWrapper</tt> itself provides default methods that
 * forward method calls to the wrapped Iteration. Subclasses of
 * <tt>IterationWrapper</tt> should override some of these methods and may
 * also provide additional methods and fields.
 */
public class IterationWrapper<E, X extends Exception> extends CloseableIterationBase<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The wrapped Iteration.
	 */
	protected final Iteration<? extends E, ? extends X> wrappedIter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new IterationWrapper that operates on the supplied Iteration.
	 * 
	 * @param iter
	 *        The wrapped Iteration for this <tt>IterationWrapper</tt>, must
	 *        not be <tt>null</tt>.
	 */
	public IterationWrapper(Iteration<? extends E, ? extends X> iter) {
		assert iter != null;
		wrappedIter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Checks whether the wrapped Iteration contains more elements, closing this
	 * Iteration when this is not the case.
	 * 
	 * @return <tt>true</tt> if the wrapped Iteration contains more elements,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean hasNext()
		throws X
	{
		return wrappedIter.hasNext();
	}

	/**
	 * Returns the next element from the wrapped Iteration.
	 * 
	 * @throws java.util.NoSuchElementException
	 *         If all elements have been returned.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed.
	 */
	public E next()
		throws X
	{
		return wrappedIter.next();
	}

	/**
	 * Removes the last element that has been returned from the wrapped
	 * Iteration.
	 * 
	 * @throws UnsupportedOperationException
	 *         If the wrapped Iteration does not support the <tt>remove</tt>
	 *         operation.
	 * @throws IllegalStateException
	 *         if the Iteration has been closed, or if {@link #next} has not yet
	 *         been called, or {@link #remove} has already been called after the
	 *         last call to {@link #next}.
	 */
	public void remove()
		throws X
	{
		wrappedIter.remove();
	}

	/**
	 * Closed this Iteration and also closes the wrapped Iteration if it is a
	 * {@link CloseableIteration}.
	 */
	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		Iterations.closeCloseable(wrappedIter);
	}
}
