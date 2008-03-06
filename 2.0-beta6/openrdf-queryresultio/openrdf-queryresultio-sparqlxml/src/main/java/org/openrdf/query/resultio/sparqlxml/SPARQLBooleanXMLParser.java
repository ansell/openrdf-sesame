/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
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

	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.SPARQL;
	}

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
