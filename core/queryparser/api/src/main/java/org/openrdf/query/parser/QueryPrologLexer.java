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
package org.openrdf.query.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A simple lexer that tokenizes a syntactically legal input SPARQL query string
 * on prolog items (prefixes, base declarations, IRIs, comments, and syntactical
 * tokens such as keywords, opening and closing brackets, and hashes).
 * 
 * @author Jeen Broekstra
 * @since 2.7.12
 */
public class QueryPrologLexer {

	public static enum TokenType {
		PREFIX_KEYWORD,
		PREFIX,
		BASE_KEYWORD,
		COLON,
		LBRACKET,
		RBRACKET,
		IRI,
		HASH,
		COMMENT,
		REST_OF_QUERY
	}

	public static class Token {

		public final TokenType t;

		public final String s;

		public Token(TokenType t, String s) {
			this.t = t;
			this.s = s;
		}

		public TokenType getType() {
			return t;
		}

		/**
		 * Get the corresponding string value for this token. For example in the
		 * case of an {@link TokenType#IRI} token, this will return the string
		 * representation of that IRI.
		 */
		public String getStringValue() {
			return s;
		}

		@Override
		public String toString() {
			return "[" + t.toString() + "] '" + s + "'";
		}
	}

	/**
	 * Tokenizes a syntactically legal input SPARQL query on prolog elements. The
	 * last token in the returned list is of type {@link TokenType#REST_OF_QUERY}
	 * and contains the SPARQL query string minus the prolog.
	 * 
	 * @param input
	 *        a syntactically legal SPARQL query string
	 * @return a list with tokens for each prolog element. If the input string is
	 *         syntactically legal SPARQL, the final returned token is guaranteed
	 *         to be of type {@link TokenType#REST_OF_QUERY} and to contain the
	 *         SPARQL query string minus the prolog.
	 */
	public static List<Token> lex(String input) {
		final List<Token> result = new ArrayList<QueryPrologLexer.Token>();
		for (int i = 0; i < input.length();) {
			char c = input.charAt(i);
			switch (c) {
				case '#':
					result.add(new Token(TokenType.HASH, "#"));
					String comment = readComment(input, i);
					i += comment.length() + 1; // 1 for hash
					result.add(new Token(TokenType.COMMENT, comment));
					break;
				case 'p':
				case 'P':
					result.add(new Token(TokenType.PREFIX_KEYWORD, "PREFIX"));
					// read PREFIX
					String prefix = readPrefix(input, i);
					result.add(new Token(TokenType.PREFIX, prefix.trim()));
					i = i + prefix.length() + 7; // 6 for prefix keyword, 1 for ':'
					break;
				case 'b':
				case 'B':
					result.add(new Token(TokenType.BASE_KEYWORD, "BASE"));
					i += 4; // 4 for base keyword
					break;
				case '<':
					// read IRI
					result.add(new Token(TokenType.LBRACKET, "<"));
					String iri = readIRI(input, i);
					result.add(new Token(TokenType.IRI, iri));
					result.add(new Token(TokenType.RBRACKET, ">"));
					i += iri.length() + 2; // 2 for opening and closing brackets
					break;
				default:
					if (Character.isWhitespace(c)) {
						i++;
					}
					else {
						String restOfQuery = input.substring(i);
						result.add(new Token(TokenType.REST_OF_QUERY, restOfQuery));
						i += restOfQuery.length();
					}
					break;
			}
		}

		return result;
	}

	private static String readComment(String input, int index) {
		String comment = null;
		Pattern pattern = Pattern.compile("^#([^\n]+)");
		Matcher matcher = pattern.matcher(input.substring(index));
		if (matcher.find()) {
			comment = matcher.group(1);
		}
		return comment;
	}

	private static String readPrefix(String input, int index) {
		String prefix = null;
		Pattern pattern = Pattern.compile("^prefix([^:]+):", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(input.substring(index));
		if (matcher.find()) {
			prefix = matcher.group(1);
		}
		return prefix;
	}

	private static String readIRI(String input, int index) {
		String iri = null;
		Pattern pattern = Pattern.compile("^<([^>]*)>*");
		;
		Matcher matcher = pattern.matcher(input.substring(index));
		if (matcher.find()) {
			iri = matcher.group(1);
		}
		return iri;
	}
}
