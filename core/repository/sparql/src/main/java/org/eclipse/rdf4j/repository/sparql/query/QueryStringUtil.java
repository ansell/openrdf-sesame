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
package org.eclipse.rdf4j.repository.sparql.query;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.util.Literals;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.parser.sparql.SPARQLUtil;

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
	 * @return the modified queryString
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
		if (value instanceof IRI) {
			return appendValue(sb, (IRI)value).toString();
		}
		else if (value instanceof Literal) {
			return appendValue(sb, (Literal)value).toString();
		}
		else {
			throw new IllegalArgumentException("BNode references not supported by SPARQL end-points");
		}
	}

	private static StringBuilder appendValue(StringBuilder sb, IRI uri) {
		sb.append("<").append(uri.stringValue()).append(">");
		return sb;
	}

	private static StringBuilder appendValue(StringBuilder sb, Literal lit) {
		sb.append('"');
		sb.append(SPARQLUtil.encodeString(lit.getLabel()));
		sb.append('"');

		if (Literals.isLanguageLiteral(lit)) {
			sb.append('@');
			sb.append(lit.getLanguage().get());
		}
		else {
			sb.append("^^<");
			sb.append(lit.getDatatype().stringValue());
			sb.append('>');
		}
		return sb;
	}
}
