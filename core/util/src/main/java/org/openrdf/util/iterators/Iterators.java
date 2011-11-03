/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */

package org.openrdf.util.iterators;

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
}
