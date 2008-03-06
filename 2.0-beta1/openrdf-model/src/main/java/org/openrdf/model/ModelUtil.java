/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.model;

/**
 * @author Arjohn Kampman
 */
public class ModelUtil {

	/**
	 * Finds the index of the first local name character in an (non-relative)
	 * URI. This index is determined by the following the following steps:
	 * <ul>
	 * <li>Find the <em>first</em> occurrence of the '#' character,
	 * <li>If this fails, find the <em>last</em> occurrence of the '/'
	 * character,
	 * <li>If this fails, find the <em>last</em> occurrence of the ':'
	 * character.
	 * <li>Add <tt>1<tt> to the found index and return this value.
	 * </ul>
	 * Note that the third step should never fail as every legal (non-relative)
	 * URI contains at least one ':' character to seperate the scheme from the
	 * rest of the URI. If this fails anyway, the method will throw an
	 * {@link IllegalArgumentException}.
	 * 
	 * @param uri
	 *        A URI string.
	 * @return The index of the first local name character in the URI string. Note that
	 * this index does not reference an actual character if the algorithm determines
	 * that there is not local name. In that case, the return index is equal to the
	 * length of the URI string.
	 * @throws IllegalArgumentException
	 *         If the supplied URI string doesn't contain any of the separator
	 *         characters. Every legal (non-relative) URI contains at least one
	 *         ':' character to seperate the scheme from the rest of the URI.
	 */
	public static int getLocalNameIndex(String uri) {
		int separatorIdx = uri.indexOf('#');

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf('/');
		}

		if (separatorIdx < 0) {
			separatorIdx = uri.lastIndexOf(':');
		}

		if (separatorIdx < 0) {
			throw new IllegalArgumentException("No separator character founds in URI: " + uri);
		}

		return separatorIdx + 1;
	}
}
