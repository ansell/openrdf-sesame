/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.queryresult.xml;

import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.BINDING_NAME_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.BINDING_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.BNODE_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.HEAD_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.LITERAL_DATATYPE_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.LITERAL_LANG_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.LITERAL_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_SET_DISTINCT_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_SET_ORDERED_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_SET_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.RESULT_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.URI_TAG;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.VAR_NAME_ATT;
import static org.openrdf.queryresult.xml.SPARQLResultsXMLConstants.VAR_TAG;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import org.openrdf.queryresult.TupleQueryResultFormat;
import org.openrdf.queryresult.TupleQueryResultHandlerException;
import org.openrdf.queryresult.TupleQueryResultParseException;
import org.openrdf.queryresult.helpers.QueryResultParserBase;
import org.openrdf.queryresult.impl.MapSolution;
import org.openrdf.util.xml.SimpleSAXAdapter;
import org.openrdf.util.xml.SimpleSAXParser;
import org.openrdf.util.xml.XMLReaderFactory;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Parser for reading query results formatted as SPARQL Results Documents. See
 * <a href="http://www.w3.org/TR/rdf-sparql-XMLres/">SPARQL Query Results XML
 * Format</a> for the definition of this format. The parser assumes that the
 * XML is wellformed.
 */
public class SPARQLResultsXMLParser extends QueryResultParserBase {

	/*-------------*
	 * Construtors *
	 *-------------*/

	/**
	 * Creates a new parser for the SPARQL Query Results XML Format that will use
	 * an instance of {@link ValueFactoryImpl} to create Value objects.
	 */
	public SPARQLResultsXMLParser() {
		super();
	}

	/**
	 * Creates a new parser for the SPARQL Query Results XML Format that will use
	 * the supplied ValueFactory to create Value objects.
	 */
	public SPARQLResultsXMLParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.SPARQL;
	}

	public void parse(InputStream in)
		throws IOException, TupleQueryResultParseException, TupleQueryResultHandlerException
	{
		try {
			SimpleSAXParser simpleSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());

			simpleSAXParser.setListener(new SPARQLResultsParser());

			simpleSAXParser.parse(in);
		}
		catch (SAXException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc instanceof TupleQueryResultParseException) {
				throw (TupleQueryResultParseException)wrappedExc;
			}
			else if (wrappedExc instanceof TupleQueryResultHandlerException) {
				throw (TupleQueryResultHandlerException)wrappedExc;
			}
			else {
				throw new TupleQueryResultParseException(wrappedExc);
			}
		}
	}

	/*---------------------------------*
	 * Inner class SPARQLResultsParser *
	 *---------------------------------*/

	private class SPARQLResultsParser extends SimpleSAXAdapter {

		/*-----------*
		 * Variables *
		 *-----------*/

		/**
		 * Flag indicating whether the query results are ordered.
		 */
		private boolean _ordered;

		/**
		 * Flag indicating whether the query results are distinct.
		 */
		private boolean _distinct;

		/**
		 * The variable names that are specified in the header.
		 */
		private List<String> _bindingNames;

		/**
		 * The most recently parsed binding name.
		 */
		private String _currentBindingName;

		/**
		 * The most recently parsed value.
		 */
		private Value _currentValue;

		/**
		 * The bound variables for the current result.
		 */
		private MapSolution _currentSolution;

		/*---------*
		 * Methods *
		 *---------*/

		public void startDocument()
			throws SAXException
		{
			_bindingNames = new ArrayList<String>();
			_ordered = false;
			_distinct = false;
			_currentValue = null;
		}

		public void endDocument()
			throws SAXException
		{
			try {
				_handler.endQueryResult();
			}
			catch (TupleQueryResultHandlerException e) {
				throw new SAXException(e);
			}
		}

		public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException
		{
			if (BINDING_TAG.equals(tagName)) {
				_currentBindingName = atts.get(BINDING_NAME_ATT);

				if (_currentBindingName == null) {
					throw new SAXException(BINDING_NAME_ATT + " attribute missing for " + BINDING_TAG + " element");
				}
			}
			else if (URI_TAG.equals(tagName)) {
				try {
					_currentValue = _valueFactory.createURI(text);
				}
				catch (IllegalArgumentException e) {
					// Malformed URI
					throw new SAXException(e.getMessage());
				}
			}
			else if (BNODE_TAG.equals(tagName)) {
				_currentValue = _valueFactory.createBNode(text);
			}
			else if (LITERAL_TAG.equals(tagName)) {
				String xmlLang = atts.get(LITERAL_LANG_ATT);
				String datatype = atts.get(LITERAL_DATATYPE_ATT);

				if (datatype != null) {
					try {
						_currentValue = _valueFactory.createLiteral(text, _valueFactory.createURI(datatype));
					}
					catch (IllegalArgumentException e) {
						// Illegal datatype URI
						throw new SAXException(e.getMessage());
					}
				}
				else if (xmlLang != null) {
					_currentValue = _valueFactory.createLiteral(text, xmlLang);
				}
				else {
					_currentValue = _valueFactory.createLiteral(text);
				}
			}
			else if (RESULT_TAG.equals(tagName)) {
				_currentSolution = new MapSolution(_bindingNames.size());
			}
			else if (VAR_TAG.equals(tagName)) {
				String varName = atts.get(VAR_NAME_ATT);

				if (varName == null) {
					throw new SAXException(VAR_NAME_ATT + " missing for " + VAR_TAG + " element");
				}

				_bindingNames.add(varName);
			}
			else if (RESULT_SET_TAG.equals(tagName)) {
				_ordered = "true".equals(atts.get(RESULT_SET_ORDERED_ATT));
				_distinct = "true".equals(atts.get(RESULT_SET_DISTINCT_ATT));
			}
		}

		public void endTag(String tagName)
			throws SAXException
		{
			if (BINDING_TAG.equals(tagName)) {
				if (_currentValue == null) {
					throw new SAXException("Value missing for " + BINDING_TAG + " element");
				}

				_currentSolution.addBinding(_currentBindingName, _currentValue);

				_currentBindingName = null;
				_currentValue = null;
			}
			else if (RESULT_TAG.equals(tagName)) {
				try {
					_handler.handleSolution(_currentSolution);
					_currentSolution = null;
				}
				catch (TupleQueryResultHandlerException e) {
					throw new SAXException(e);
				}
			}
			else if (HEAD_TAG.equals(tagName)) {
				try {
					_handler.startQueryResult(_bindingNames, _distinct, _ordered);
				}
				catch (TupleQueryResultHandlerException e) {
					throw new SAXException(e);
				}
			}
		}
	}
}
