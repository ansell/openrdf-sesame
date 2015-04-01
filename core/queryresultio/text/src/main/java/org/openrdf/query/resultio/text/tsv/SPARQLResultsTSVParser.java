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
package org.openrdf.query.resultio.text.tsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Literal;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParser;
import org.openrdf.query.resultio.TupleQueryResultParserBase;

/**
 * SPARQL Results TSV format parser.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsTSVParser extends TupleQueryResultParserBase implements TupleQueryResultParser {

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.TSV;
	}

	@Override
	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException
	{
		InputStreamReader r = new InputStreamReader(in, Charset.forName("UTF-8"));

		BufferedReader reader = new BufferedReader(r);

		List<String> bindingNames = null;

		String nextLine;
		while ((nextLine = reader.readLine()) != null) {
			if (bindingNames == null) {
				// header is mandatory in SPARQL TSV
				String[] names = nextLine.split("\t", -1);
				bindingNames = new ArrayList<String>(names.length);
				for (String name : names) {
					// strip the '?' prefix
					if ('?' == name.charAt(0)) {
						bindingNames.add(name.substring(1));
					} else {
						bindingNames.add(name);
					}
				}
				if (handler != null) {
					handler.startQueryResult(bindingNames);
				}
			}
			else {
				// process solution
				String[] lineTokens = nextLine.split("\t", -1);
				List<Value> values = new ArrayList<Value>();
				for (String valueString : lineTokens) {
					Value v = null;
					if (valueString.startsWith("_:")) {
						v = valueFactory.createBNode(valueString.substring(2));
					}
					else if (valueString.startsWith("<") && valueString.endsWith(">")) {
						try {
							v = valueFactory.createIRI(valueString.substring(1, valueString.length() - 1));
						}
						catch (IllegalArgumentException e) {
							v = valueFactory.createLiteral(valueString);
						}
					}
					else if (valueString.startsWith("\"")) {
						v = parseLiteral(valueString);
					}
					else if (!"".equals(valueString)) {
						if (valueString.matches("^[\\+\\-]?[\\d\\.].*")) {

							IRI datatype = null;

							if (XMLDatatypeUtil.isValidInteger(valueString)) {
								if (XMLDatatypeUtil.isValidNegativeInteger(valueString)) {
									datatype = XMLSchema.NEGATIVE_INTEGER;
								}
								else {
									datatype = XMLSchema.INTEGER;
								}
							}
							else if (XMLDatatypeUtil.isValidDecimal(valueString)) {
								datatype = XMLSchema.DECIMAL;
							}
							else if (XMLDatatypeUtil.isValidDouble(valueString)) {
								datatype = XMLSchema.DOUBLE;
							}

							if (datatype != null) {
								v = valueFactory.createLiteral(valueString, datatype);
							}
							else {
								v = valueFactory.createLiteral(valueString);
							}
						}
						else {
							v = valueFactory.createLiteral(valueString);
						}
					}
					values.add(v);
				}

				BindingSet bindingSet = new ListBindingSet(bindingNames, values.toArray(new Value[values.size()]));
				if (handler != null) {
					handler.handleSolution(bindingSet);
				}
			}
		}

		if (bindingNames != null && handler != null) {
			handler.endQueryResult();
		}
	}

	/**
	 * Parses a literal, creates an object for it and returns this object.
	 * 
	 * @param literal
	 *        The literal to parse.
	 * @return An object representing the parsed literal.
	 * @throws IllegalArgumentException
	 *         If the supplied literal could not be parsed correctly.
	 */
	protected Literal parseLiteral(String literal)
		throws IllegalArgumentException
	{
		if (literal.startsWith("\"")) {
			// Find string separation points
			int endLabelIdx = findEndOfLabel(literal);

			if (endLabelIdx != -1) {
				int startLangIdx = literal.indexOf("@", endLabelIdx);
				int startDtIdx = literal.indexOf("^^", endLabelIdx);

				if (startLangIdx != -1 && startDtIdx != -1) {
					throw new IllegalArgumentException("Literals can not have both a language and a datatype");
				}

				// Get label
				String label = literal.substring(1, endLabelIdx);
				label = decodeString(label);

				if (startLangIdx != -1) {
					// Get language
					String language = literal.substring(startLangIdx + 1);
					return valueFactory.createLiteral(label, language);
				}
				else if (startDtIdx != -1) {
					// Get datatype
					String datatype = literal.substring(startDtIdx + 2);
					datatype = datatype.substring(1, datatype.length() - 1);
					IRI dtURI = valueFactory.createIRI(datatype);
					return valueFactory.createLiteral(label, dtURI);
				}
				else {
					return valueFactory.createLiteral(label);
				}
			}
		}

		throw new IllegalArgumentException("Not a legal literal: " + literal);
	}

	/**
	 * Finds the end of the label in a literal string.
	 * 
	 * @return The index of the double quote ending the label.
	 */
	private int findEndOfLabel(String literal) {
		// we just look for the last occurrence of a double quote
		return literal.lastIndexOf("\"");
	}

	/**
	 * Decodes an encoded Turtle string. Any \-escape sequences are substituted
	 * with their decoded value.
	 * 
	 * @param s
	 *        An encoded Turtle string.
	 * @return The unencoded string.
	 * @exception IllegalArgumentException
	 *            If the supplied string is not a correctly encoded Turtle
	 *            string.
	 **/
	public static String decodeString(String s) {
		int backSlashIdx = s.indexOf('\\');

		if (backSlashIdx == -1) {
			// No escaped characters found
			return s;
		}

		int startIdx = 0;
		int sLength = s.length();
		StringBuilder sb = new StringBuilder(sLength);

		while (backSlashIdx != -1) {
			sb.append(s.substring(startIdx, backSlashIdx));

			if (backSlashIdx + 1 >= sLength) {
				throw new IllegalArgumentException("Unescaped backslash in: " + s);
			}

			char c = s.charAt(backSlashIdx + 1);

			if (c == 't') {
				sb.append('\t');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'r') {
				sb.append('\r');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'n') {
				sb.append('\n');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '"') {
				sb.append('"');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '>') {
				sb.append('>');
				startIdx = backSlashIdx + 2;
			}
			else if (c == '\\') {
				sb.append('\\');
				startIdx = backSlashIdx + 2;
			}
			else if (c == 'u') {
				// \\uxxxx
				if (backSlashIdx + 5 >= sLength) {
					throw new IllegalArgumentException("Incomplete Unicode escape sequence in: " + s);
				}
				String xx = s.substring(backSlashIdx + 2, backSlashIdx + 6);

				try {
					c = (char)Integer.parseInt(xx, 16);
					sb.append(c);

					startIdx = backSlashIdx + 6;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal Unicode escape sequence '\\u" + xx + "' in: " + s);
				}
			}
			else if (c == 'U') {
				// \\Uxxxxxxxx
				if (backSlashIdx + 9 >= sLength) {
					throw new IllegalArgumentException("Incomplete Unicode escape sequence in: " + s);
				}
				String xx = s.substring(backSlashIdx + 2, backSlashIdx + 10);

				try {
					c = (char)Integer.parseInt(xx, 16);
					sb.append(c);

					startIdx = backSlashIdx + 10;
				}
				catch (NumberFormatException e) {
					throw new IllegalArgumentException("Illegal Unicode escape sequence '\\U" + xx + "' in: " + s);
				}
			}
			else {
				throw new IllegalArgumentException("Unescaped backslash in: " + s);
			}

			backSlashIdx = s.indexOf('\\', startIdx);
		}

		sb.append(s.substring(startIdx));

		return sb.toString();
	}
}
