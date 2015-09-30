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

package org.eclipse.rdf4j.common.iteration;

/**
 * A CloseableIteration that converts an iteration over objects of type
 * <tt>S</tt> (the source type) to an iteration over objects of type
 * <tt>T</tt> (the target type).
 */
public abstract class ConvertingIteration<S, T, X extends Exception> extends AbstractCloseableIteration<T, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The source type iteration.
	 */
	private final Iteration<? extends S, ? extends X> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ConvertingIteration that operates on the supplied source
	 * type iteration.
	 * 
	 * @param iter
	 *        The source type iteration for this <tt>ConvertingIteration</tt>,
	 *        must not be <tt>null</tt>.
	 */
	public ConvertingIteration(Iteration<? extends S, ? extends X> iter) {
		assert iter != null;
		this.iter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Converts a source type object to a target type object.
	 */
	protected abstract T convert(S sourceObject)
		throws X;

	/**
	 * Checks whether the source type iteration contains more elements.
	 * 
	 * @return <tt>true</tt> if the source type iteration contains more
	 *         elements, <tt>false</tt> otherwise.
	 * @throws X
	 */
	public boolean hasNext()
		throws X
	{
		return iter.hasNext();
	}

	/**
	 * Returns the next element from the source type iteration.
	 * 
	 * @throws X
	 * @throws java.util.NoSuchElementException
	 *         If all elements have been returned.
	 * @throws IllegalStateException
	 *         If the iteration has been closed.
	 */
	public T next()
		throws X
	{
		return convert(iter.next());
	}

	/**
	 * Calls <tt>remove()</tt> on the underlying Iteration.
	 * 
	 * @throws UnsupportedOperationException
	 *         If the wrapped Iteration does not support the <tt>remove</tt>
	 *         operation.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed, or if {@link #next} has not yet
	 *         been called, or {@link #remove} has already been called after the
	 *         last call to {@link #next}.
	 */
	public void remove()
		throws X
	{
		iter.remove();
	}

	/**
	 * Closes this iteration as well as the wrapped iteration if it is a
	 * {@link CloseableIteration}.
	 */
	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		Iterations.closeCloseable(iter);
	}
}
