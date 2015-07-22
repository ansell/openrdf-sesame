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
package org.openrdf.query.resultio.sparqlxml;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultParseException;

/**
 * Parser for reading boolean query results formatted as SPARQL Results
 * Documents. See <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query
 * Results XML Format</a> for the definition of this format. The parser assumes
 * that the XML is wellformed.
 */
public class SPARQLBooleanXMLParser extends SPARQLXMLParserBase implements BooleanQueryResultParser {

	/*-------------*
	 * Construtors *
	 *-------------*/

	/**
	 * Creates a new parser for the SPARQL Query Results XML Format.
	 */
	public SPARQLBooleanXMLParser() {
		super();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
		return getBooleanQueryResultFormat();
	}

	@Override
	public synchronized boolean parse(InputStream in)
		throws IOException, QueryResultParseException
	{
		try {
			return parseQueryResultInternal(in, true, false);
		}
		catch (QueryResultHandlerException e) {
			throw new QueryResultParseException(e);
		}
	}

	/*---------------------------------*
	 * Inner class SPARQLBooleanParser *
	 *---------------------------------*/
}
