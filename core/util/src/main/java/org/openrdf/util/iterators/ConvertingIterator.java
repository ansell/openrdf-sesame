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
