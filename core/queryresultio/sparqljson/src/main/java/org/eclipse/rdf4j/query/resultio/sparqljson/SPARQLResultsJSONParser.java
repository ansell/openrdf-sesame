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
package org.eclipse.rdf4j.query.resultio.sparqljson;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.TupleQueryResultHandler;
import org.eclipse.rdf4j.query.TupleQueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultParseException;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.TupleQueryResultParser;
import org.eclipse.rdf4j.query.resultio.helpers.QueryResultCollector;

/**
 * Parser for SPARQL-1.1 JSON Results Format documents
 * 
 * @see <a href="http://www.w3.org/TR/sparql11-results-json/">SPARQL 1.1 Query
 *      Results JSON Format</a>
 * @author Peter Ansell
 */
public class SPARQLResultsJSONParser extends AbstractSPARQLJSONParser implements TupleQueryResultParser {

	/**
	 * Default constructor.
	 */
	public SPARQLResultsJSONParser() {
		super();
	}

	/**
	 * Construct a parser with a specific {@link ValueFactory}.
	 * 
	 * @param valueFactory
	 *        The factory to use to create values.
	 */
	public SPARQLResultsJSONParser(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
		return getTupleQueryResultFormat();
	}

	@Override
	public TupleQueryResultFormat getTupleQueryResultFormat() {
		return TupleQueryResultFormat.JSON;
	}

	@Override
	@Deprecated
	public void setTupleQueryResultHandler(TupleQueryResultHandler handler) {
		setQueryResultHandler(handler);
	}

	@Override
	@Deprecated
	public void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException
	{
		try {
			parseQueryResultInternal(in, false, true);
		}
		catch (TupleQueryResultHandlerException e) {
			throw e;
		}
		catch (QueryResultHandlerException e) {
			throw new TupleQueryResultHandlerException(e);
		}
	}

}
