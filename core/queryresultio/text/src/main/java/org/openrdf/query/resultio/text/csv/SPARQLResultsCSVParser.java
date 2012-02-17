/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

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
 * SPARQL Results CSV format parser.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVParser extends TupleQueryResultParserBase {

	private List<String> bindingNames;

	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.CSV;
	}

	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException
	{
		CSVReader reader = new CSVReader(new InputStreamReader(in, Charset.forName("UTF-8")));
		String[] nextLine;
		while ((nextLine = reader.readNext()) != null) {
			if (bindingNames == null) {
				// header is mandatory in SPARQL CSV
				bindingNames = Arrays.asList(nextLine);
				handler.startQueryResult(bindingNames);
			}
			else {
				// process solution
				List<Value> values = new ArrayList<Value>();
				for (String valueString : nextLine) {
					Value v = null;
					if (valueString.startsWith("_:")) {
						v = valueFactory.createBNode(valueString.substring(2));
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
							try {
								v = valueFactory.createURI(valueString);
							}
							catch (IllegalArgumentException e) {
								v = valueFactory.createLiteral(valueString);
							}
						}
					}
					values.add(v);
				}

				BindingSet bindingSet = new ListBindingSet(bindingNames, values.toArray(new Value[values.size()]));
				handler.handleSolution(bindingSet);
			}
		}
	}

}
