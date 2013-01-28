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
package org.openrdf.model.util;

import org.openrdf.model.URI;

/**
 * @author Arjohn Kampman
 */
public class URIUtil {

	/**
	 * Finds the index of the first local name character in an (non-relative)
	 * URI. This index is determined by the following the following steps:
	 * <ul>
	 * <li>Find the <em>first</em> occurrence of the '#' character,
	 * <li>If this fails, find the <em>last</em> occurrence of the '/' character,
	 * <li>If this fails, find the <em>last</em> occurrence of the ':' character.
	 * <li>Add <tt>1<tt> to the found index and return this value.
	 * </ul>
	 * Note that the third step should never fail as every legal (non-relative)
	 * URI contains at least one ':' character to seperate the scheme from the
	 * rest of the URI. If this fails anyway, the method will throw an
	 * {@link IllegalArgumentException}.
	 * 
	 * @param uri
	 *        A URI string.
	 * @return The index of the first local name character in the URI string.
	 *         Note that this index does not reference an actual character if the
	 *         algorithm determines that there is not local name. In that case,
	 *         the return index is equal to the length of the URI string.
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

	/**
	 * Checks whether the URI consisting of the specified namespace and local
	 * name has been split correctly according to the URI splitting rules
	 * specified in {@link URI}.
	 * 
	 * @param namespace
	 *        The URI's namespace, must not be <tt>null</tt>.
	 * @param localName
	 *        The URI's local name, must not be <tt>null</tt>.
	 * @return <tt>true</tt> if the specified URI has been correctly split into a
	 *         namespace and local name, <tt>false</tt> otherwise.
	 * @see URI
	 * @see #getLocalNameIndex(String)
	 */
	public static boolean isCorrectURISplit(String namespace, String localName) {
		assert namespace != null : "namespace must not be null";
		assert localName != null : "localName must not be null";

		if (namespace.length() == 0) {
			return false;
		}

		int nsLength = namespace.length();
		char lastNsChar = namespace.charAt(nsLength - 1);

		if (lastNsChar == '#') {
			// correct split if namespace has no other '#'
			return namespace.lastIndexOf('#', nsLength - 2) == -1;
		}
		else if (lastNsChar == '/') {
			// correct split if local name has no '/' and URI contains no '#'
			return localName.indexOf('/') == -1 && localName.indexOf('#') == -1 && namespace.indexOf('#') == -1;
		}
		else if (lastNsChar == ':') {
			// correct split if local name has no ':' and URI contains no '#' or
			// '/'
			return localName.indexOf(':') == -1 && localName.indexOf('#') == -1 && localName.indexOf('/') == -1
					&& namespace.indexOf('#') == -1 && namespace.indexOf('/') == -1;
		}

		return false;
	}
}
