/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXAdapter;
import info.aduna.xml.SimpleSAXParser;


import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.RDFParserBase;

import static org.openrdf.rio.trix.TriXConstants.*;

/**
 * A parser that can parse RDF files that are in the
 * <a href="http://www.w3.org/2004/03/trix/">TriX format</a>.
 */
public class TriXParser extends RDFParserBase {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriXParser that will use a {@link ValueFactoryImpl} to
	 * create objects for resources, bNodes, literals and statements.
	 */
	public TriXParser() {
		super();
	}

	/**
	 * Creates a new TriXParser that will use the supplied ValueFactory to
	 * create objects for resources, bNodes, literals and statements.
	 *
	 * @param valueFactory A ValueFactory.
	 */
	public TriXParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	// implements RDFParser.getRDFFormat()
	public final RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	// implements RDFParser.parse(...)
	public void parse(InputStream in, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		_parse(in);
	}

	// implements RDFParser.parse(...)
	public void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		_parse(reader);
	}

	private void _parse(Object inputStreamOrReader)
		throws IOException, RDFParseException, RDFHandlerException
	{
		try {
			_rdfHandler.startRDF();

			SimpleSAXParser saxParser = new SimpleSAXParser();
			saxParser.setListener(new TriXSAXHandler());

			if (inputStreamOrReader instanceof InputStream) {
				saxParser.parse( (InputStream)inputStreamOrReader );
			}
			else {
				saxParser.parse( (Reader)inputStreamOrReader );
			}

			_rdfHandler.endRDF();
		}
		catch (SAXException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc instanceof RDFParseException) {
				throw (RDFParseException)wrappedExc;
			}
			else if (wrappedExc instanceof RDFHandlerException) {
				throw (RDFHandlerException)wrappedExc;
			}
			else {
				_reportFatalError(wrappedExc);
			}
		}
	}

	/*----------------------------*
	 * Inner class TriXSAXHandler *
	 *----------------------------*/

	private class TriXSAXHandler extends SimpleSAXAdapter {

		private Resource _currentContext;

		private boolean _parsingContext;

		private List<Value> _valueList;

		public TriXSAXHandler() {
			_currentContext = null;
			_valueList = new ArrayList<Value>(3);
		}

		public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException
		{
			try {
				if (tagName.equals(URI_TAG)) {
					_valueList.add( _createURI(text) );
				}
				else if (tagName.equals(BNODE_TAG)) {
					_valueList.add( _createBNode(text) );
				}
				else if (tagName.equals(PLAIN_LITERAL_TAG)) {
					String lang = atts.get(LANGUAGE_ATT);
					_valueList.add( _createLiteral(text, lang, null) );
				}
				else if (tagName.equals(TYPED_LITERAL_TAG)) {
					String datatype = atts.get(DATATYPE_ATT);

					if (datatype == null) {
						_reportError(DATATYPE_ATT + " attribute missing for typed literal");
						_valueList.add( _createLiteral(text, null, null) );
					}
					else {
						URI dtURI = _createURI(datatype);
						_valueList.add( _createLiteral(text, null, dtURI) );
					}
				}
				else if (tagName.equals(TRIPLE_TAG)) {
					if (_parsingContext) {
						try {
							// First triple in a context, _valueList can contain context information
							if (_valueList.size() > 1) {
								_reportError("At most 1 resource can be specified for the context");
							}
							else if (_valueList.size() == 1) {
								try {
									_currentContext = (Resource)_valueList.get(0);
								}
								catch (ClassCastException e) {
									_reportError("Context identifier should be a URI or blank node");
								}
							}
						}
						finally {
							_parsingContext = false;
							_valueList.clear();
						}
					}
				}
				else if (tagName.equals(CONTEXT_TAG)) {
					_parsingContext = true;
				}
			}
			catch (RDFParseException e) {
				throw new SAXException(e);
			}
		}

		public void endTag(String tagName)
			throws SAXException
		{
			try {
				if (tagName.equals(TRIPLE_TAG)) {
					_reportStatement();
				}
				else if (tagName.equals(CONTEXT_TAG)) {
					_currentContext = null;
				}
			}
			catch (RDFParseException e) {
				throw new SAXException(e);
			}
			catch (RDFHandlerException e) {
				throw new SAXException(e);
			}
		}

		private void _reportStatement()
			throws RDFParseException, RDFHandlerException
		{
			try {
				if (_valueList.size() != 3) {
					_reportError("exactly 3 values are required for a triple");
					return;
				}

				Resource subj;
				URI pred;
				Value obj;

				try {
					subj = (Resource)_valueList.get(0);
				}
				catch (ClassCastException e) {
					_reportError("First value for a triple should be a URI or blank node");
					return;
				}

				try {
					pred = (URI)_valueList.get(1);
				}
				catch (ClassCastException e) {
					_reportError("Second value for a triple should be a URI");
					return;
				}

				obj = _valueList.get(2);

				Statement st = _createStatement(subj, pred, obj, _currentContext);
				_rdfHandler.handleStatement(st);
			}
			finally {
				_valueList.clear();
			}
		}
	} // end inner class TriXSAXHandler
}
