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
package org.openrdf;

import org.openrdf.model.Resource;

/**
 * General utility methods for OpenRDF/Sesame modules.
 * 
 * @author Arjohn Kampman
 */
public class OpenRDFUtil {

	/**
	 * Verifies that the supplied contexts parameter is not <tt>null</tt>,
	 * throwing an {@link IllegalArgumentException} if it is.
	 * <p>
	 * The semantics of supplying <tt>null</tt> as the value of the
	 * <tt>contexts</tt> vararg is not completely clear; it can either be
	 * equivalent to supplying an empty array (i.e.: matching all statements
	 * disregarding context), or to supplying a <tt>null</tt>-Resource value
	 * (e.g.: matching all statements with no associated context). As we so far
	 * haven't been able to prefer one over the other, methods operating on
	 * contexts currently throw {@link IllegalArgumentException}s.
	 * 
	 * @param contexts
	 *        The parameter to check.
	 * @throws IllegalArgumentException
	 *         If the supplied contexts parameter is <tt>null</tt>.
	 */
	public static void verifyContextNotNull(Resource... contexts) {
		if (contexts == null) {
			throw new IllegalArgumentException(
					"Illegal value null array for contexts argument; either the value should be cast to Resource or an empty array should be supplied");
		}
	}
}
