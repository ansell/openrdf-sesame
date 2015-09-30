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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * This class consists exclusively of static methods that operate on or return
 * Iterations. It is the Iteration-equivalent of <tt>java.util.Collections</tt>.
 */
public class Iterations {

	/**
	 * Get a List containing all elements obtained from the specified Iteration.
	 * 
	 * @param iter
	 *        the Iteration to get the elements from
	 * @return a List containing all elements obtained from the specified
	 *         Iteration.
	 */
	public static <E, X extends Exception> List<E> asList(Iteration<? extends E, X> iter)
		throws X
	{
		List<E> result = new ArrayList<E>();
		addAll(iter, result);
		return result;
	}

	/**
	 * Get a Set containing all elements obtained from the specified Iteration.
	 * 
	 * @param iter
	 *        the Iteration to get the elements from
	 * @return a Set containing all elements obtained from the specified
	 *         Iteration.
	 */
	public static <E, X extends Exception> Set<E> asSet(Iteration<? extends E, X> iter)
		throws X
	{
		Set<E> result = new HashSet<E>();
		addAll(iter, result);
		return result;
	}

	/**
	 * Adds all elements from the supplied Iteration to the specified collection.
	 * If the supplied Iteration is an instance of {@link CloseableIteration} it
	 * is automatically closed after consumption.
	 * 
	 * @param iter
	 *        An Iteration containing elements to add to the container. If the
	 *        Iteration is an instance of {@link CloseableIteration} it is
	 *        automatically closed after consumption.
	 * @param collection
	 *        The collection to add the elements to.
	 * @return The <tt>collection</tt> object that was supplied to this method.
	 */
	public static <E, X extends Exception, C extends Collection<E>> C addAll(Iteration<? extends E, X> iter,
			C collection)
		throws X
	{
		try {
			while (iter.hasNext()) {
				collection.add(iter.next());
			}
		}
		finally {
			closeCloseable(iter);
		}

		return collection;
	}

	/**
	 * Get a sequential {@link Stream} with the supplied {@link Iteration} as its
	 * source. If the source iteration is a {@link CloseableIteration}, it will
	 * be automatically closed by the stream when done. Any checked exceptions
	 * thrown at any point during stream processing will be propagated wrapped in
	 * a {@link RuntimeException}.
	 * 
	 * @param iteration
	 *        a source {@link Iteration} for the stream.
	 * @return a sequential {@link Stream} object which can be used to process
	 *         the data from the source iteration.
	 * @since 4.0
	 */
	public static <T> Stream<T> stream(Iteration<T, ? extends Exception> iteration) {
		Spliterator<T> spliterator = new IterationSpliterator<T>(iteration);

		return StreamSupport.stream(spliterator, false).onClose(() -> {
			try {
				Iterations.closeCloseable(iteration);
			}
			catch (RuntimeException e) {
				throw e;
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
		});
	}

	/**
	 * Closes the supplied Iteration if it is an instance of
	 * {@link CloseableIteration}, otherwise the request is ignored.
	 * 
	 * @param iter
	 *        The Iteration that should be closed.
	 */
	public static <X extends Exception> void closeCloseable(Iteration<?, X> iter)
		throws X
	{
		if (iter instanceof CloseableIteration<?, ?>) {
			((CloseableIteration<?, X>)iter).close();
		}
	}

	/**
	 * Converts an Iteration to a string by concatenating all of the string
	 * representations of objects in the Iteration, divided by a separator.
	 * 
	 * @param iter
	 *        An Iteration over arbitrary objects that are expected to implement
	 *        {@link Object#toString()}.
	 * @param separator
	 *        The separator to insert between the object strings.
	 * @return A String representation of the objects provided by the supplied
	 *         Iteration.
	 */
	public static <X extends Exception> String toString(Iteration<?, X> iter, String separator)
		throws X
	{
		StringBuilder sb = new StringBuilder();
		toString(iter, separator, sb);
		return sb.toString();
	}

	/**
	 * Converts an Iteration to a string by concatenating all of the string
	 * representations of objects in the Iteration, divided by a separator.
	 * 
	 * @param iter
	 *        An Iteration over arbitrary objects that are expected to implement
	 *        {@link Object#toString()}.
	 * @param separator
	 *        The separator to insert between the object strings.
	 * @param sb
	 *        A StringBuilder to append the Iteration string to.
	 */
	public static <X extends Exception> void toString(Iteration<?, X> iter, String separator, StringBuilder sb)
		throws X
	{
		while (iter.hasNext()) {
			sb.append(iter.next());

			if (iter.hasNext()) {
				sb.append(separator);
			}
		}

	}
}