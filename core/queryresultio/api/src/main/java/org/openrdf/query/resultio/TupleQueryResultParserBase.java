/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.TupleQueryResultHandler;

/**
 * Base class for {@link TupleQueryResultParser}s offering common functionality
 * for query result parsers.
 */
public abstract class TupleQueryResultParserBase implements TupleQueryResultParser {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The ValueFactory to use for creating RDF model objects.
	 */
	protected ValueFactory valueFactory;

	/**
	 * The TupleQueryResultHandler that will handle the parsed query results.
	 */
	protected TupleQueryResultHandler handler;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new parser base that, by default, will use an instance of
	 * {@link ValueFactoryImpl} to create Value objects.
	 */
	public TupleQueryResultParserBase() {
		this(new ValueFactoryImpl());
	}

	/**
	 * Creates a new parser base that will use the supplied ValueFactory to
	 * create Value objects.
	 */
	public TupleQueryResultParserBase(ValueFactory valueFactory) {
		setValueFactory(valueFactory);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	public void setTupleQueryResultHandler(TupleQueryResultHandler handler) {
		this.handler = handler;
	}
}
