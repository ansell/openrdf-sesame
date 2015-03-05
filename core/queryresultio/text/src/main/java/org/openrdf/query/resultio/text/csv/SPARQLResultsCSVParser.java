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
package org.openrdf.query.resultio.text.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

import org.openrdf.model.URI;
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
 * SPARQL Results CSV format parser.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVParser extends TupleQueryResultParserBase implements TupleQueryResultParser {

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.CSV;
	}

	@Override
	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException
	{
		CSVReader reader = new CSVReader(new InputStreamReader(in, Charset.forName("UTF-8")));

		List<String> bindingNames = null;

		String[] nextLine;

		try {
			while ((nextLine = reader.readNext()) != null) {
				if (bindingNames == null) {
					// header is mandatory in SPARQL CSV
					bindingNames = Arrays.asList(nextLine);
					if (handler != null) {
						handler.startQueryResult(bindingNames);
					}
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

					BindingSet bindingSet = new ListBindingSet(bindingNames,
							values.toArray(new Value[values.size()]));
					if (handler != null) {
						handler.handleSolution(bindingSet);
					}
				}
			}

			if (bindingNames != null && handler != null) {
				handler.endQueryResult();
			}
		}
		finally {
			reader.close();
		}
	}
}
