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

package org.eclipse.rdf4j.util.iterators;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * This class consists exclusively of static methods that operate on or return
 * iterators. It is the Iterator-equivalent of {@link Collections}.
 */
public class Iterators {

	/**
	 * Get a List containing all elements obtained from the specified iterator.
	 * 
	 * @param iter
	 *        the iterator to get the elements from
	 * @return a List containing all elements obtained from the specified
	 *         iterator.
	 */
	public static <E> List<E> asList(Iterator<? extends E> iter) {
		List<E> result = new ArrayList<E>();
		addAll(iter, result);
		return result;
	}

	/**
	 * Adds all elements from the supplied iterator to the specified collection.
	 * 
	 * @param iter
	 *        An iterator containing elements to add to the container.
	 * @param collection
	 *        The collection to add the elements to.
	 * @return The <tt>collection</tt> object that was supplied to this method.
	 */
	public static <E, C extends Collection<E>> C addAll(Iterator<? extends E> iter, C collection) {
		while (iter.hasNext()) {
			collection.add(iter.next());
		}

		return collection;
	}

	/**
	 * Converts an iterator to a string by concatenating all of the string
	 * representations of objects in the iterator, divided by a separator.
	 * 
	 * @param iter
	 *        An iterator over arbitrary objects that are expected to implement
	 *        {@link Object#toString()}.
	 * @param separator
	 *        The separator to insert between the object strings.
	 * @return A String representation of the objects provided by the supplied
	 *         iterator.
	 */
	public static String toString(Iterator<?> iter, String separator) {
		StringBuilder sb = new StringBuilder();
		toString(iter, separator, sb);
		return sb.toString();
	}

	/**
	 * Converts an iterator to a string by concatenating all of the string
	 * representations of objects in the iterator, divided by a separator.
	 * 
	 * @param iter
	 *        An iterator over arbitrary objects that are expected to implement
	 *        {@link Object#toString()}.
	 * @param separator
	 *        The separator to insert between the object strings.
	 * @param sb
	 *        A StringBuilder to append the iterator string to.
	 */
	public static void toString(Iterator<?> iter, String separator, StringBuilder sb) {
		while (iter.hasNext()) {
			sb.append(iter.next());

			if (iter.hasNext()) {
				sb.append(separator);
			}
		}
	}

	/**
	 * Closes the given iterator if it implements {@link java.io.Closeable}
	 * else do nothing.
	 * @param iter The iterator to close.
	 * @throws IOException If an underlying I/O error occurs.
	 */
	public static void close(Iterator<?> iter) throws IOException
	{
		if(iter instanceof Closeable)
		{
			((Closeable)iter).close();
		}
	}

	/**
	 * Closes the given iterator, swallowing any IOExceptions, if it implements {@link java.io.Closeable}
	 * else do nothing.
	 * @param iter The iterator to close.
	 */
	public static void closeSilently(Iterator<?> iter)
	{
		if(iter instanceof Closeable)
		{
			try
			{
				((Closeable)iter).close();
			}
			catch(IOException ioe)
			{
				// ignore
			}
		}
	}
}
