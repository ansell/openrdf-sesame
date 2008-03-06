/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import info.aduna.lang.service.ServiceRegistry;

/**
 * A registry that keeps track of the available
 * {@link TupleQueryResultWriterFactory}s.
 * 
 * @author Arjohn Kampman
 */
public class TupleQueryResultWriterRegistry extends
		ServiceRegistry<TupleQueryResultFormat, TupleQueryResultWriterFactory>
{

	public TupleQueryResultWriterRegistry() {
		super(TupleQueryResultWriterFactory.class);
	}

	protected TupleQueryResultFormat getKey(TupleQueryResultWriterFactory factory) {
		return factory.getTupleQueryResultFormat();
	}
}
