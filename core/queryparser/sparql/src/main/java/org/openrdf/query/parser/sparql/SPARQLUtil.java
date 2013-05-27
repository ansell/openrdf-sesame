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
package org.openrdf.query.parser.sparql;

import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * SPARQL-related utility methods.
 * 
 * @author Arjohn Kampman
 */
public class SPARQLUtil {

	/**
	 * Encodes the supplied string for inclusion as a 'normal' string in a SPARQL
	 * query.
	 */
	public static String encodeString(String s) {
		return NTriplesUtil.escapeString(s);
	}

	/**
	 * Decodes an encoded SPARQL string. Any \-escape sequences are substituted
	 * with their decoded value.
	 * 
	 * @param s
	 *        An encoded SPARQL string.
	 * @return The unencoded string.
	 * @exception IllegalArgumentException
	 *            If the supplied string is not a correctly encoded SPARQL
	 *            string.
	 */
	public static String decodeString(String s) {
		return NTriplesUtil.unescapeString(s);
	}
}
