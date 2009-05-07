/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.text;

import java.io.OutputStream;

import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;

/**
 * A {@link BooleanQueryResultWriterFactory} for writers of plain text boolean
 * query results.
 * 
 * @author Arjohn Kampman
 */
public class BooleanTextWriterFactory implements BooleanQueryResultWriterFactory {

	/**
	 * Returns {@link BooleanQueryResultFormat#TEXT}.
	 */
	public BooleanQueryResultFormat getBooleanQueryResultFormat() {
		return BooleanQueryResultFormat.TEXT;
	}

	/**
	 * Returns a new instance of BinaryQueryResultWriter.
	 */
	public BooleanQueryResultWriter getWriter(OutputStream out) {
		return new BooleanTextWriter(out);
	}
}
