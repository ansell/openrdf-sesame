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
