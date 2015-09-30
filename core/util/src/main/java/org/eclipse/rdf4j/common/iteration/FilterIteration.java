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

import java.util.NoSuchElementException;

/**
 * A CloseableIteration that wraps another Iteration, applying a filter on the
 * objects that are returned. Subclasses must implement the <tt>accept</tt>
 * method to indicate which objects should be returned.
 */
public abstract class FilterIteration<E, X extends Exception> extends IterationWrapper<E, X> {

	/*-----------*
	 * Variables *
	 *-----------*/

	private E nextElement;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * @param iter
	 */
	public FilterIteration(Iteration<? extends E, ? extends X> iter) {
		super(iter);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public boolean hasNext()
		throws X
	{
		findNextElement();

		return nextElement != null;
	}

	@Override
	public E next()
		throws X
	{
		findNextElement();

		E result = nextElement;

		if (result != null) {
			nextElement = null;
			return result;
		}
		else {
			throw new NoSuchElementException();
		}
	}

	private void findNextElement()
		throws X
	{
		while (nextElement == null && super.hasNext()) {
			E candidate = super.next();

			if (accept(candidate)) {
				nextElement = candidate;
			}
		}
	}

	/**
	 * Tests whether or not the specified object should be returned by this
	 * Iteration. All objects from the wrapped Iteration pass through this method
	 * in the same order as they are coming from the wrapped Iteration.
	 * 
	 * @param object
	 *        The object to be tested.
	 * @return <tt>true</tt> if the object should be returned, <tt>false</tt>
	 *         otherwise.
	 * @throws X
	 */
	protected abstract boolean accept(E object)
		throws X;

	@Override
	protected void handleClose()
		throws X
	{
		super.handleClose();
		nextElement = null;
	}
}
