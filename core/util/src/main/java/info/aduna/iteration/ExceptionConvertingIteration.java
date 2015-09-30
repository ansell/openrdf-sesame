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

import java.util.NoSuchElementException;

/**
 * A CloseableIteration that converts an arbitrary iteration to an iteration
 * with exceptions of type <tt>X</tt>. Subclasses need to override
 * {@link #convert(Exception)} to do the conversion.
 */
public abstract class ExceptionConvertingIteration<E, X extends Exception> extends
		AbstractCloseableIteration<E, X>
{

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The underlying Iteration.
	 */
	private final Iteration<? extends E, ? extends Exception> iter;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new ExceptionConvertingIteration that operates on the supplied
	 * iteration.
	 * 
	 * @param iter
	 *        The Iteration that this <tt>ExceptionConvertingIteration</tt>
	 *        operates on, must not be <tt>null</tt>.
	 */
	public ExceptionConvertingIteration(Iteration<? extends E, ? extends Exception> iter) {
		assert iter != null;
		this.iter = iter;
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Converts an exception from the underlying iteration to an exception of
	 * type <tt>X</tt>.
	 */
	protected abstract X convert(Exception e);

	/**
	 * Checks whether the underlying Iteration contains more elements.
	 * 
	 * @return <tt>true</tt> if the underlying Iteration contains more
	 *         elements, <tt>false</tt> otherwise.
	 * @throws X
	 */
	public boolean hasNext()
		throws X
	{
		try {
			return iter.hasNext();
		}
		catch (Exception e) {
			throw convert(e);
		}
	}

	/**
	 * Returns the next element from the wrapped Iteration.
	 * 
	 * @throws X
	 * @throws java.util.NoSuchElementException
	 *         If all elements have been returned.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed.
	 */
	public E next()
		throws X
	{
		try {
			return iter.next();
		}
		catch (NoSuchElementException e) {
			throw e;
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			throw convert(e);
		}
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
		try {
			iter.remove();
		}
		catch (UnsupportedOperationException e) {
			throw e;
		}
		catch (IllegalStateException e) {
			throw e;
		}
		catch (Exception e) {
			throw convert(e);
		}
	}

	/**
	 * Closes this Iteration as well as the wrapped Iteration if it happens to be
	 * a {@link CloseableIteration}.
	 */
	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();

		try {
			Iterations.closeCloseable(iter);
		}
		catch (Exception e) {
			throw convert(e);
		}
	}
}
