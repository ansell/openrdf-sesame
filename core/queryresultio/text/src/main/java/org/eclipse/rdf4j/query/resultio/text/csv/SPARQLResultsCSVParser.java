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
package org.eclipse.rdf4j.query.resultio.text.csv;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.opencsv.CSVReader;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.datatypes.XMLDatatypeUtil;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.impl.ListBindingSet;
import org.eclipse.rdf4j.query.resultio.AbstractTupleQueryResultParser;
import org.eclipse.rdf4j.query.resultio.QueryResultParseException;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultParser;

/**
 * SPARQL Results CSV format parser.
 * 
 * @author Jeen Broekstra
 */
public class SPARQLResultsCSVParser extends AbstractTupleQueryResultParser implements TupleQueryResultParser {

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
								try {
									v = valueFactory.createIRI(valueString);
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
