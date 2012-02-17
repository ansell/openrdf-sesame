/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text.tsv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import info.aduna.text.StringUtil;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.vocabulary.XMLSchema;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultParserBase;

/**
 * SPARQL Results TSV format parser.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsTSVParser extends TupleQueryResultParserBase {

	private List<String> bindingNames;

	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.TSV;
	}

	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException
	{
		InputStreamReader r = new InputStreamReader(in, Charset.forName("UTF-8"));

		BufferedReader reader = new BufferedReader(r);

		String nextLine;
		while ((nextLine = reader.readLine()) != null) {
			if (bindingNames == null) {
				// header is mandatory in SPARQL TSV
				String[] names = nextLine.split("\t", -1);
				bindingNames = new ArrayList<String>(names.length);
				for (String name: names) {
					// strip the '?' prefix
					bindingNames.add(name.substring(1));
				}
				handler.startQueryResult(bindingNames);
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
					else if (valueString.startsWith("<")) {
						try {
							v = valueFactory.createURI(valueString.substring(1, valueString.length() - 1));
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

							URI datatype = null;

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
				handler.handleSolution(bindingSet);
			}
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
				label = unescapeString(label);

				if (startLangIdx != -1) {
					// Get language
					String language = literal.substring(startLangIdx + 1);
					return valueFactory.createLiteral(label, language);
				}
				else if (startDtIdx != -1) {
					// Get datatype
					String datatype = literal.substring(startDtIdx + 2);
					datatype = datatype.substring(1, datatype.length() - 1);
					URI dtURI = valueFactory.createURI(datatype);
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

	private String unescapeString(String s) {
		s = StringUtil.gsub("\\", "", s);
		return s;
	}
}
