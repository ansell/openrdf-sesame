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
package org.openrdf.query.resultio.sparqljson;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import info.aduna.io.UncloseableInputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.query.resultio.QueryResultParserBase;

/**
 * Abstract base class for SPARQL Results JSON Parsers.
 * 
 * @author Peter Ansell
 */
public abstract class SPARQLJSONParserBase extends QueryResultParserBase {

	/**
	 * 
	 */
	public SPARQLJSONParserBase() {
		super();
	}

	/**
	 * 
	 */
	public SPARQLJSONParserBase(ValueFactory valueFactory) {
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
		UncloseableInputStream uncloseable = new UncloseableInputStream(buff);

		try {
			buff.mark(Integer.MAX_VALUE);
			boolean result = false;

			// Try to parse it as a boolean result
			
			// Reset the buffered input stream and try again looking for tuple
			// results
			buff.reset();

			// If it failed to parse as a boolean result, try to parse it as a tuple result
			
			return result;
		}
		finally {
			uncloseable.doClose();
		}
	}

}