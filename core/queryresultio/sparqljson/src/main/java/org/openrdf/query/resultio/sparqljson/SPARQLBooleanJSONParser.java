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
package org.openrdf.query.resultio.sparqljson;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.helpers.QueryResultCollector;

/**
 * Parser for SPARQL-1.1 JSON Results Format documents
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-results-json/">SPARQL 1.1 Query
 *      Results JSON Format</a>
 * @author Peter Ansell
 */
public class SPARQLBooleanJSONParser extends SPARQLJSONParserBase implements BooleanQueryResultParser {

	/**
	 * Default constructor.
	 */
	public SPARQLBooleanJSONParser() {
		super();
	}

	/**
	 * Construct a parser with a specific {@link ValueFactory}.
	 * 
	 * @param valueFactory
	 *        The factory to use to create values.
	 */
	public SPARQLBooleanJSONParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
		return getBooleanQueryResultFormat();
	}

	@Override
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.JSON;
	}

	@Override
	@Deprecated
	public boolean parse(InputStream in)
		throws IOException, QueryResultParseException
	{
		try {
			if (handler == null) {
				handler = new QueryResultCollector();
			}
			return parseQueryResultInternal(in);
		}
		catch (QueryResultHandlerException e) {
			throw new QueryResultParseException(e);
		}
	}

}
