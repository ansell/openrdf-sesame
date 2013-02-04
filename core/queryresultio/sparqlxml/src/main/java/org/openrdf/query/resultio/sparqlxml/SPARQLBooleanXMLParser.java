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

import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_FALSE;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TAG;
import static org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLConstants.BOOLEAN_TRUE;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;
import info.aduna.xml.SimpleSAXParser;
import info.aduna.xml.XMLReaderFactory;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.QueryResultParseException;

/**
 * Parser for reading boolean query results formatted as SPARQL Results
 * Documents. See <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query
 * Results XML Format</a> for the definition of this format. The parser assumes
 * that the XML is wellformed.
 */
public class SPARQLBooleanXMLParser implements BooleanQueryResultParser {

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
	public boolean parse(InputStream in)
		throws IOException, QueryResultParseException
	{
		try {
			SPARQLBooleanParser valueParser = new SPARQLBooleanParser();

			SimpleSAXParser simpleSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());
			simpleSAXParser.setListener(valueParser);
			simpleSAXParser.parse(in);

			return valueParser.getValue();
		}
		catch (SAXException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc instanceof QueryResultParseException) {
				throw (QueryResultParseException)wrappedExc;
			}
			else {
				throw new QueryResultParseException(wrappedExc);
			}
		}
	}

	/*---------------------------------*
	 * Inner class SPARQLBooleanParser *
	 *---------------------------------*/

	private static class SPARQLBooleanParser extends SimpleSAXAdapter {

		/*-----------*
		 * Variables *
		 *-----------*/

		private Boolean value;

		/*---------*
		 * Methods *
		 *---------*/

		@Override
		public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException
		{
			if (BOOLEAN_TAG.equals(tagName)) {
				if (BOOLEAN_TRUE.equals(text)) {
					value = true;
				}
				else if (BOOLEAN_FALSE.equals(text)) {
					value = false;
				}
				else {
					throw new SAXException("Illegal value for element " + BOOLEAN_TAG + ": " + text);
				}
			}
		}

		@Override
		public void endDocument()
			throws SAXException
		{
			if (value == null) {
				throw new SAXException("Malformed document, " + BOOLEAN_TAG + " element not found");
			}
		}

		public boolean getValue() {
			return value != null && value;
		}
	}
}
