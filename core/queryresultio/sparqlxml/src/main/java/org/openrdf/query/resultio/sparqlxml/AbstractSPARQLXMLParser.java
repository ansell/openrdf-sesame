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
package org.openrdf.query.resultio.sparqlxml;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import info.aduna.io.UncloseableInputStream;
import info.aduna.xml.SimpleSAXParser;
import info.aduna.xml.XMLReaderFactory;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.AbstractQueryResultParser;

/**
 * Abstract base class for SPARQL Results XML Parsers.
 * 
 * @author Peter Ansell
 */
public abstract class AbstractSPARQLXMLParser extends AbstractQueryResultParser {

	/**
	 * 
	 */
	public AbstractSPARQLXMLParser() {
		super();
	}

	/**
	 * 
	 */
	public AbstractSPARQLXMLParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public void parseQueryResult(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		parseQueryResultInternal(in, true, true);
	}

	protected boolean parseQueryResultInternal(InputStream in, boolean attemptParseBoolean,
			boolean attemptParseTuple)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		if (!attemptParseBoolean && !attemptParseTuple) {
			throw new IllegalArgumentException(
					"Internal error: Did not specify whether to parse as either boolean and/or tuple");
		}

		BufferedInputStream buff = new BufferedInputStream(in);
		UncloseableInputStream uncloseable = new UncloseableInputStream(buff);

		SAXException caughtException = null;

		boolean result = false;

		try {
			if (attemptParseBoolean) {
				buff.mark(Integer.MAX_VALUE);
				try {
					SPARQLBooleanSAXParser valueParser = new SPARQLBooleanSAXParser();

					SimpleSAXParser booleanSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());
					booleanSAXParser.setListener(valueParser);
					booleanSAXParser.parse(uncloseable);

					result = valueParser.getValue();

					try {
						if (this.handler != null) {
							this.handler.handleBoolean(result);
						}
					}
					catch (QueryResultHandlerException e) {
						if (e.getCause() != null && e.getCause() instanceof IOException) {
							throw (IOException)e.getCause();
						}
						else {
							throw new QueryResultParseException("Found an issue with the query result handler", e);
						}
					}
					// if there were no exceptions up to this point, return the
					// boolean
					// result;
					return result;
				}
				catch (SAXException e) {
					caughtException = e;
				}

				// Reset the buffered input stream and try again looking for tuple
				// results
				buff.reset();
			}

			if (attemptParseTuple) {
				try {
					SimpleSAXParser resultsSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());
					resultsSAXParser.setPreserveWhitespace(true);

					resultsSAXParser.setListener(new SPARQLResultsSAXParser(this.valueFactory, this.handler));

					resultsSAXParser.parse(uncloseable);

					// we had success, so remove the exception that we were tracking
					// from
					// the boolean failure
					caughtException = null;
				}
				catch (SAXException e) {
					caughtException = e;
				}
			}

			if (caughtException != null) {
				Exception wrappedExc = caughtException.getException();

				if (wrappedExc == null) {
					throw new QueryResultParseException(caughtException);
				}
				else if (wrappedExc instanceof QueryResultParseException) {
					throw (QueryResultParseException)wrappedExc;
				}
				else if (wrappedExc instanceof QueryResultHandlerException) {
					throw (QueryResultHandlerException)wrappedExc;
				}
				else {
					throw new QueryResultParseException(wrappedExc);
				}
			}

		}
		finally {
			uncloseable.doClose();
		}
		
		return result;
	}

}