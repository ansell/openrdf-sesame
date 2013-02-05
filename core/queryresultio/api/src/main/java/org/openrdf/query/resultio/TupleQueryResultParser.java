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
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

/**
 * A general interface for tuple query result parsers.
 */
public interface TupleQueryResultParser {

	/**
	 * Gets the query result format that this parser can parse.
	 */
	TupleQueryResultFormat getTupleQueryResultFormat();

	/**
	 * Sets the ValueFactory that the parser will use to create Value objects for
	 * the parsed query result.
	 * 
	 * @param valueFactory
	 *        The value factory that the parser should use.
	 */
	void setValueFactory(ValueFactory valueFactory);

	/**
	 * Sets the TupleQueryResultHandler that will handle the parsed query result
	 * data.
	 */
	void setTupleQueryResultHandler(TupleQueryResultHandler handler);

	/**
	 * Parses the data from the supplied InputStream.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws QueryResultParseException
	 *         If the parser has encountered an unrecoverable parse error.
	 * @throws TupleQueryResultHandlerException
	 *         If the configured query result handler has encountered an
	 *         unrecoverable error.
	 */
	void parse(InputStream in)
		throws IOException, QueryResultParseException, TupleQueryResultHandlerException;
}
