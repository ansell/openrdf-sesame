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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * An Iteration that can convert an {@link Iterator} to a
 * {@link CloseableIteration}.
 */
public class CloseableIteratorIteration<E, X extends Exception> extends AbstractCloseableIteration<E, X> {

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
