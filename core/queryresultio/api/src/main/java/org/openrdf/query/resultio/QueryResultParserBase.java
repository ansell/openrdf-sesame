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

import java.io.InputStream;

import org.xml.sax.SAXException;

import info.aduna.xml.SimpleSAXParser;
import info.aduna.xml.XMLReaderFactory;

import org.openrdf.query.QueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;

/**
 * Base class for {@link QueryResultParser}s offering common functionality for
 * query result parsers.
 */
public abstract class QueryResultParserBase implements QueryResultParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The {@link ValueFactory} to use for creating RDF model objects.
	 */
	protected ValueFactory valueFactory;

	/**
	 * The {@link QueryResultHandler} that will handle the parsed query results.
	 */
	protected QueryResultHandler handler;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser base that, by default, will use an instance of
	 * {@link ValueFactoryImpl} to create Value objects.
	 */
	public QueryResultParserBase() {
		this(new ValueFactoryImpl());
	}

	/**
	 * Creates a new parser base that will use the supplied ValueFactory to
	 * create Value objects.
	 */
	public QueryResultParserBase(ValueFactory valueFactory) {
		setValueFactory(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	@Override
	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	@Override
	public void setQueryResultHandler(QueryResultHandler handler) {
		this.handler = handler;
	}
	
	
}
