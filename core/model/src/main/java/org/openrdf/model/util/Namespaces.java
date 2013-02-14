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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Namespace;

/**
 * A utility class to perform operations on {@link Namespace}s.
 * 
 * @author Peter Ansell
 * @since 2.7.0
 */
public class Namespaces {

	/**
	 * Converts a set of {@link Namespace}s into a map containing the
	 * {@link Namespace#getPrefix()} strings as keys, with the
	 * {@link Namespace#getName()} strings as values in the map for each
	 * namespace in the given set.
	 * 
	 * @param namespaces
	 *        The {@link Set} of {@link Namespace}s to transform.
	 * @return A {@link Map} of {@link String} to {@link String} where the
	 *         key/value combinations are created based on the prefix and names
	 *         from {@link Namespace}s in the input set.
	 * @since 2.7.0
	 */
	public static Map<String, String> asMap(Set<Namespace> namespaces) {
		Map<String, String> result = new HashMap<String, String>();

		for (Namespace nextNamespace : namespaces) {
			result.put(nextNamespace.getPrefix(), nextNamespace.getName());
		}

		return result;
	}

	private Namespaces() {
		// private default constructor, this is a static class
	}

}
