/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio;

import java.io.OutputStream;

import org.openrdf.query.TupleQueryResultHandler;

/**
 * The interface of objects that writer query results in a specific query result
 * format.
 */
public interface TupleQueryResultWriter extends TupleQueryResultHandler {

	/**
	 * Gets the query result format that this writer uses.
	 */
	public TupleQueryResultFormat getQueryResultFormat();

	public void setOutputStream(OutputStream out);
}
