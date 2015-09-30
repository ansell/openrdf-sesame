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
 * An Iteration is a typed Iterator-like object that can throw (typed)
 * Exceptions while iterating. This is used in cases where the iteration is lazy
 * and evaluates over a (remote) connection, for example accessing a database.
 * In such cases an error can occur at any time and needs to be communicated
 * through a checked exception, something {@link java.util.Iterator} can not do
 * (it can only throw {@link RuntimeException}s.
 * 
 * @param <E>
 *        Object type of objects contained in the iteration.
 * @param <X>
 *        Exception type that is thrown when a problem occurs during iteration.
 * @see java.util.Iterator
 * @author jeen
 * @author Herko ter Horst
 */
public interface Iteration<E, X extends Exception> {

	/**
	 * Returns <tt>true</tt> if the iteration has more elements. (In other
	 * words, returns <tt>true</tt> if {@link #next} would return an element
	 * rather than throwing a <tt>NoSuchElementException</tt>.)
	 * 
	 * @return <tt>true</tt> if the iteration has more elements.
	 * @throws X
	 */
	public boolean hasNext()
		throws X;

	/**
	 * Returns the next element in the iteration.
	 * 
	 * @return the next element in the iteration.
	 * @throws NoSuchElementException
	 *         if the iteration has no more elements or if it has been closed.
	 */
	public E next()
		throws X;

	/**
	 * Removes from the underlying collection the last element returned by the
	 * iteration (optional operation). This method can be called only once per
	 * call to next.
	 * 
	 * @throws UnsupportedOperationException
	 *         if the remove operation is not supported by this Iteration.
	 * @throws IllegalStateException
	 *         If the Iteration has been closed, or if <tt>next()</tt> has not
	 *         yet been called, or <tt>remove()</tt> has already been called
	 *         after the last call to <tt>next()</tt>.
	 */
	public void remove()
		throws X;
}
