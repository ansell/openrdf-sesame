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
package org.openrdf.rio.trix;

import static org.openrdf.rio.trix.TriXConstants.BNODE_TAG;
import static org.openrdf.rio.trix.TriXConstants.CONTEXT_TAG;
import static org.openrdf.rio.trix.TriXConstants.DATATYPE_ATT;
import static org.openrdf.rio.trix.TriXConstants.LANGUAGE_ATT;
import static org.openrdf.rio.trix.TriXConstants.PLAIN_LITERAL_TAG;
import static org.openrdf.rio.trix.TriXConstants.TRIPLE_TAG;
import static org.openrdf.rio.trix.TriXConstants.TYPED_LITERAL_TAG;
import static org.openrdf.rio.trix.TriXConstants.URI_TAG;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.input.BOMInputStream;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import info.aduna.xml.SimpleSAXAdapter;
import info.aduna.xml.SimpleSAXParser;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.IRI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.helpers.AbstractRDFParser;
import org.openrdf.rio.helpers.TriXParserSettings;

/**
 * A parser that can parse RDF files that are in the <a
 * href="http://www.w3.org/2004/03/trix/">TriX format</a>.
 * 
 * @author Arjohn Kampman
 */
public class TriXParser extends AbstractRDFParser {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new TriXParser that will use a {@link SimpleValueFactory} to
	 * create objects for resources, bNodes, literals and statements.
	 */
	public TriXParser() {
		super();
	}

	/**
	 * Creates a new TriXParser that will use the supplied ValueFactory to create
	 * objects for resources, bNodes, literals and statements.
	 * 
	 * @param valueFactory
	 *        A ValueFactory.
	 */
	public TriXParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public final RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	public void parse(InputStream in, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		parse(new BOMInputStream(in, false));
	}

	public void parse(Reader reader, String baseURI)
		throws IOException, RDFParseException, RDFHandlerException
	{
		parse(reader);
	}

	private void parse(Object inputStreamOrReader)
		throws IOException, RDFParseException, RDFHandlerException
	{
		if (rdfHandler != null) {
			rdfHandler.startRDF();
		}

		try {
			SimpleSAXParser saxParser = new SimpleSAXParser();
			saxParser.setPreserveWhitespace(true);
			saxParser.setListener(new TriXSAXHandler());

			if (inputStreamOrReader instanceof InputStream) {
				saxParser.parse((InputStream)inputStreamOrReader);
			}
			else {
				saxParser.parse((Reader)inputStreamOrReader);
			}
		}
		catch (SAXParseException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc == null) {
				reportFatalError(e, e.getLineNumber(), e.getColumnNumber());
			}
			else {
				reportFatalError(wrappedExc, e.getLineNumber(), e.getColumnNumber());
			}
		}
		catch (SAXException e) {
			Exception wrappedExc = e.getException();

			if (wrappedExc == null) {
				reportFatalError(e);
			}
			else if (wrappedExc instanceof RDFParseException) {
				throw (RDFParseException)wrappedExc;
			}
			else if (wrappedExc instanceof RDFHandlerException) {
				throw (RDFHandlerException)wrappedExc;
			}
			else {
				reportFatalError(wrappedExc);
			}
		}
		finally {
			clear();
		}
		
		if (rdfHandler != null) {
			rdfHandler.endRDF();
		}
	}

	/*----------------------------*
	 * Inner class TriXSAXHandler *
	 *----------------------------*/

	private class TriXSAXHandler extends SimpleSAXAdapter {

		private Resource currentContext;

		private boolean parsingContext;

		private List<Value> valueList;

		public TriXSAXHandler() {
			currentContext = null;
			valueList = new ArrayList<Value>(3);
		}

		@Override
		public void startTag(String tagName, Map<String, String> atts, String text)
			throws SAXException
		{
			try {
				if (tagName.equals(URI_TAG)) {
					valueList.add(createURI(text));
				}
				else if (tagName.equals(BNODE_TAG)) {
					valueList.add(createBNode(text));
				}
				else if (tagName.equals(PLAIN_LITERAL_TAG)) {
					String lang = atts.get(LANGUAGE_ATT);
					valueList.add(createLiteral(text, lang, null));
				}
				else if (tagName.equals(TYPED_LITERAL_TAG)) {
					String datatype = atts.get(DATATYPE_ATT);

					if (datatype == null) {
						reportError(DATATYPE_ATT + " attribute missing for typed literal",
								TriXParserSettings.FAIL_ON_TRIX_MISSING_DATATYPE);
						valueList.add(createLiteral(text, null, null));
					}
					else {
						IRI dtURI = createURI(datatype);
						valueList.add(createLiteral(text, null, dtURI));
					}
				}
				else if (tagName.equals(TRIPLE_TAG)) {
					if (parsingContext) {
						try {
							// First triple in a context, valueList can contain
							// context information
							if (valueList.size() > 1) {
								reportError("At most 1 resource can be specified for the context",
										TriXParserSettings.FAIL_ON_TRIX_INVALID_STATEMENT);
							}
							else if (valueList.size() == 1) {
								try {
									currentContext = (Resource)valueList.get(0);
								}
								catch (ClassCastException e) {
									reportError("Context identifier should be a URI or blank node",
											TriXParserSettings.FAIL_ON_TRIX_INVALID_STATEMENT);
								}
							}
						}
						finally {
							parsingContext = false;
							valueList.clear();
						}
					}
				}
				else if (tagName.equals(CONTEXT_TAG)) {
					parsingContext = true;
				}
			}
			catch (RDFParseException e) {
				throw new SAXException(e);
			}
		}

		@Override
		public void endTag(String tagName)
			throws SAXException
		{
			try {
				if (tagName.equals(TRIPLE_TAG)) {
					reportStatement();
				}
				else if (tagName.equals(CONTEXT_TAG)) {
					currentContext = null;
				}
			}
			catch (RDFParseException e) {
				throw new SAXException(e);
			}
			catch (RDFHandlerException e) {
				throw new SAXException(e);
			}
		}

		private void reportStatement()
			throws RDFParseException, RDFHandlerException
		{
			try {
				if (valueList.size() != 3) {
					reportError("exactly 3 values are required for a triple",
							TriXParserSettings.FAIL_ON_TRIX_INVALID_STATEMENT);
					return;
				}

				Resource subj;
				IRI pred;
				Value obj;

				try {
					subj = (Resource)valueList.get(0);
				}
				catch (ClassCastException e) {
					reportError("First value for a triple should be a URI or blank node",
							TriXParserSettings.FAIL_ON_TRIX_INVALID_STATEMENT);
					return;
				}

				try {
					pred = (IRI)valueList.get(1);
				}
				catch (ClassCastException e) {
					reportError("Second value for a triple should be a URI",
							TriXParserSettings.FAIL_ON_TRIX_INVALID_STATEMENT);
					return;
				}

				obj = valueList.get(2);

				Statement st = createStatement(subj, pred, obj, currentContext);
				if (rdfHandler != null) {
					rdfHandler.handleStatement(st);
				}
			}
			finally {
				valueList.clear();
			}
		}
	} // end inner class TriXSAXHandler
}
