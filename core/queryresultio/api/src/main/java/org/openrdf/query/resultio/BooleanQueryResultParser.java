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

/**
 * A general interface for boolean query result parsers.
 * 
 * @author Arjohn Kampman
 */
public interface BooleanQueryResultParser {

	/**
	 * Gets the query result format that this parser can parse.
	 */
	BooleanQueryResultFormat getBooleanQueryResultFormat();

	/**
	 * Parses the data from the supplied InputStream.
	 * 
	 * @param in
	 *        The InputStream from which to read the data.
	 * @throws IOException
	 *         If an I/O error occurred while data was read from the InputStream.
	 * @throws QueryResultParseException
	 *         If the parser has encountered an unrecoverable parse error.
	 */
	boolean parse(InputStream in)
		throws IOException, QueryResultParseException;
}
