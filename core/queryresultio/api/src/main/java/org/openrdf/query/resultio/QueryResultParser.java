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
package org.openrdf.query.resultio;

import java.io.IOException;
import java.io.InputStream;

import org.openrdf.model.ValueFactory;
import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.QueryResultHandlerException;

/**
 * Base interface for parsers of query results in both boolean and tuple forms.
 * 
 * @author Peter Ansell p_ansell@yahoo.com
 * @since 2.7.0
 */
public interface QueryResultParser {

	/**
	 * Gets the query result format that this parser can parse.
	 * 
	 * @return The {@link QueryResultFormat} supported by this parser.
	 * @since 2.7.0
	 */
	QueryResultFormat getQueryResultFormat();
	
	/**
	 * Sets the {@link QueryResultHandler} to be used when parsing query results
	 * using {@link #parseQueryResult(InputStream)}.
	 * 
	 * @param handler
	 *        The {@link QueryResultHandler} to use for handling results.
	 * @since 2.7.0
	 */
	void setQueryResultHandler(QueryResultHandler handler);

	/**
	 * Sets the ValueFactory that the parser will use to create Value objects for
	 * the parsed query result.
	 * 
	 * @param valueFactory
	 *        The value factory that the parser should use.
	 */
	void setValueFactory(ValueFactory valueFactory);

	/**
	 * Parse the query results out of the given {@link InputStream} into the
	 * handler setup using {@link #setQueryResultHandler(QueryResultHandler)}.
	 * 
	 * @param in
	 *        The {@link InputStream} to parse the results from.
	 * @throws IOException
	 *         If there is an exception from the InputStream.
	 * @throws QueryResultParseException
	 *         If the query results are not parsable by this parser.
	 * @throws QueryResultHandlerException
	 *         If the {@link QueryResultHandler} set in
	 *         {@link #setQueryResultHandler(QueryResultHandler)} throws an
	 *         exception.
	 * @since 2.7.0
	 */
	void parseQueryResult(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException;
}
