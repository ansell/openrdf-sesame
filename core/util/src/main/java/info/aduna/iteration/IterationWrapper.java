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

package info.aduna.iteration;

/**
 * Abstract superclass for Iterations that wrap other Iterations. The abstract
 * class <tt>IterationWrapper</tt> itself provides default methods that
 * forward method calls to the wrapped Iteration. Subclasses of
 * <tt>IterationWrapper</tt> should override some of these methods and may
 * also provide additional methods and fields.
 */
public class IterationWrapper<E, X extends Exception> extends AbstractCloseableIteration<E, X> {

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
