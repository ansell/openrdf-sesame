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
package org.openrdf.repository.sparql.query;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.parser.sparql.SPARQLUtil;
import org.openrdf.rio.ntriples.NTriplesUtil;

/**
 * Utility class to perfom query string manipulations as used in
 * {@link SPARQLTupleQuery}, {@link SPARQLGraphQuery} and
 * {@link SPARQLBooleanQuery}.
 * 
 * @author Andreas Schwarte
 * @see SPARQLTupleQuery
 * @see SPARQLGraphQuery
 * @see SPARQLBooleanQuery
 */
public class QueryStringUtil {

	// TODO maybe add BASE declaration here as well?

	/**
	 * Retrieve a modified queryString into which all bindings of the given
	 * argument are replaced.
	 * 
	 * @param queryString
	 * @param bindings
	 * @return
	 */
	public static String getQueryString(String queryString, BindingSet bindings) {
		if (bindings.size() == 0) {
			return queryString;
		}

		String qry = queryString;
		int b = qry.indexOf('{');
		String select = qry.substring(0, b);
		String where = qry.substring(b);
		for (String name : bindings.getBindingNames()) {
			String replacement = getReplacement(bindings.getValue(name));
			if (replacement != null) {
				String pattern = "[\\?\\$]" + name + "(?=\\W)";
				select = select.replaceAll(pattern, "(" + Matcher.quoteReplacement(replacement) + " as ?" + name
						+ ")");

				// we use Matcher.quoteReplacement to make sure things like newlines
				// in literal values
				// are preserved
				where = where.replaceAll(pattern, Matcher.quoteReplacement(replacement));
			}
		}
		return select + where;
	}

	private static String getReplacement(Value value) {
		StringBuilder sb = new StringBuilder();
		if (value instanceof URI) {
			return appendValue(sb, (URI)value).toString();
		}
		else if (value instanceof Literal) {
			return appendValue(sb, (Literal)value).toString();
		}
		else {
			throw new IllegalArgumentException("BNode references not supported by SPARQL end-points");
		}
	}

	private static StringBuilder appendValue(StringBuilder sb, URI uri) {
		try {
			NTriplesUtil.append(uri, sb);
			return sb;
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to serialize URI", e);
		}
	}

	private static StringBuilder appendValue(StringBuilder sb, Literal lit) {
		try {
			NTriplesUtil.append(lit, sb);
			return sb;
		}
		catch (IOException e) {
			throw new RuntimeException("Failed to serialize literal", e);
		}
	}
}
