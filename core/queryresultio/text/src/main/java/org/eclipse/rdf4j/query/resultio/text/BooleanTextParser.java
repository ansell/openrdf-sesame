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
package org.eclipse.rdf4j.query.resultio.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import org.eclipse.rdf4j.common.io.IOUtil;
import org.eclipse.rdf4j.query.QueryResultHandler;
import org.eclipse.rdf4j.query.QueryResultHandlerException;
import org.eclipse.rdf4j.query.resultio.AbstractQueryResultParser;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultFormat;
import org.eclipse.rdf4j.query.resultio.BooleanQueryResultParser;
import org.eclipse.rdf4j.query.resultio.QueryResultFormat;
import org.eclipse.rdf4j.query.resultio.QueryResultParseException;

/**
 * Reader for the plain text boolean result format.
 */
public class BooleanTextParser extends AbstractQueryResultParser implements BooleanQueryResultParser {

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser for the plain text boolean query result format.
	 */
	public BooleanTextParser() {
		super();
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public final BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.TEXT;
	}

	@Override
	public synchronized boolean parse(InputStream in)
		throws IOException, QueryResultParseException
	{
		Reader reader = new InputStreamReader(in, Charset.forName("US-ASCII"));
		String value = IOUtil.readString(reader, 16);
		value = value.trim();

		boolean result = false;

		if (value.equalsIgnoreCase("true")) {
			result = true;
		}
		else if (value.equalsIgnoreCase("false")) {
			result = false;
		}
		else {
			throw new QueryResultParseException("Invalid value: " + value);
		}

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

		return result;
	}

	@Override
	public final QueryResultFormat getQueryResultFormat() {
		return getBooleanQueryResultFormat();
	}

	@Override
	public void parseQueryResult(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		parse(in);
	}
}
