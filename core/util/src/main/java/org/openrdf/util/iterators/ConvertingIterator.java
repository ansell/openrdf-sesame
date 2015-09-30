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

package org.openrdf.util.iterators;

import java.util.Iterator;

/**
 * An Iterator that converts an iterator over objects of type <tt>S</tt>
 * (the source type) to an iterator over objects of type <tt>T</tt> (the target
 * type).
 */
public abstract class ConvertingIterator<S, T> implements Iterator<T> {

	/**
	 * The source type iterator.
	 */
	private final Iterator<? extends S> sourceIter;

	/**
	 * Creates a new ConvertingIterator that operates on the supplied source type
	 * itertor.
	 * 
	 * @param iter
	 *        The source type itertor for this <tt>ConvertingIterator</tt>, must
	 *        not be <tt>null</tt>.
	 */
	public ConvertingIterator(Iterator<? extends S> iter) {
		assert iter != null;
		this.sourceIter = iter;
	}

	/**
	 * Converts a source type object to a target type object.
	 */
	protected abstract T convert(S sourceObject);

	/**
	 * Checks whether the source type itertor contains more elements.
	 * 
	 * @return <tt>true</tt> if the source type itertor contains more elements,
	 *         <tt>false</tt> otherwise.
	 */
	public boolean hasNext() {
		return sourceIter.hasNext();
	}

	/**
	 * Returns the next element from the source type itertor.
	 * 
	 * @throws java.util.NoSuchElementException
	 *         If all elements have been returned.
	 * @throws IllegalStateException
	 *         If the itertor has been closed.
	 */
	public T next() {
		return convert(sourceIter.next());
	}

	/**
	 * Calls <tt>remove()</tt> on the underlying itertor.
	 * 
	 * @throws UnsupportedOperationException
	 *         If the wrapped itertor does not support the <tt>remove</tt>
	 *         operation.
	 * @throws IllegalStateException
	 *         If the itertor has been closed, or if {@link #next} has not yet
	 *         been called, or {@link #remove} has already been called after the
	 *         last call to {@link #next}.
	 */
	public void remove() {
		sourceIter.remove();
	}
}
