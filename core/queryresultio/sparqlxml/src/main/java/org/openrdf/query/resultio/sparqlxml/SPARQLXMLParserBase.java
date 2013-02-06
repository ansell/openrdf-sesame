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

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXParser;
import info.aduna.xml.XMLReaderFactory;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.QueryResultParserBase;

/**
 * Abstract base class for SPARQL Results XML Parsers.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 */
public abstract class SPARQLXMLParserBase extends QueryResultParserBase {

	/**
	 * 
	 */
	public SPARQLXMLParserBase() {
		super();
	}

	/**
	 * 
	 */
	public SPARQLXMLParserBase(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public void parseQueryResult(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		parseQueryResultInternal(in);
	}

	protected boolean parseQueryResultInternal(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		BufferedInputStream buff = new BufferedInputStream(in);
		buff.mark(Integer.MAX_VALUE);
		boolean result = false;

		SAXException caughtException = null;

		try {
			SPARQLBooleanSAXParser valueParser = new SPARQLBooleanSAXParser();

			SimpleSAXParser booleanSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());
			booleanSAXParser.setListener(valueParser);
			booleanSAXParser.parse(buff);

			result = valueParser.getValue();

			if (this.handler != null) {
				try {
					this.handler.handleBoolean(result);
				}
				catch (QueryResultHandlerException e) {
					if (e.getCause() != null && e.getCause() instanceof IOException) {
						throw (IOException)e.getCause();
					}
					else {
						throw new QueryResultParseException("Found an issue with the query result handler", e);
					}
				}
			}
			// if there were no exceptions up to this point, return the boolean
			// result;
			return result;
		}
		catch (SAXException e) {
			caughtException = e;
		}

		// Reset the buffered input stream and try again looking for tuple results
		buff.reset();

		try {
			SimpleSAXParser resultsSAXParser = new SimpleSAXParser(XMLReaderFactory.createXMLReader());

			resultsSAXParser.setListener(new SPARQLResultsSAXParser(this.valueFactory, this.handler));

			resultsSAXParser.parse(buff);

			// we had success, so remove the exception that we were tracking from
			// the boolean failure
			caughtException = null;
		}
		catch (SAXException e) {
			caughtException = e;
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

		return result;
	}

}