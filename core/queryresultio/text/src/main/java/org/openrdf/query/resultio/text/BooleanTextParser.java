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
package org.openrdf.query.resultio.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import info.aduna.io.IOUtil;

import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultParser;
import org.openrdf.query.resultio.QueryResultFormat;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.QueryResultParserBase;

/**
 * Reader for the plain text boolean result format.
 */
public class BooleanTextParser extends QueryResultParserBase implements BooleanQueryResultParser {

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
