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

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.TupleQueryResultHandler;

/**
 * Base class for {@link TupleQueryResultParser}s offering common functionality
 * for query result parsers.
 */
public abstract class TupleQueryResultParserBase extends QueryResultParserBase implements
		TupleQueryResultParser
{

	/**
	 * Creates a new parser base that, by default, will use an instance of
	 * {@link ValueFactoryImpl} to create Value objects.
	 */
	public TupleQueryResultParserBase() {
		super();
	}

	/**
	 * Creates a new parser base that will use the supplied {@link ValueFactory}
	 * to create {@link Value} objects.
	 */
	public TupleQueryResultParserBase(ValueFactory valueFactory) {
		super(valueFactory);
	}

	@Override
	public QueryResultFormat getQueryResultFormat() {
		return getTupleQueryResultFormat();
	}

	@Override
	public void parseQueryResult(InputStream in)
		throws IOException, QueryResultParseException, QueryResultHandlerException
	{
		parse(in);
	}

	@Override
	public void setTupleQueryResultHandler(TupleQueryResultHandler handler) {
		setQueryResultHandler(handler);
	}

}
