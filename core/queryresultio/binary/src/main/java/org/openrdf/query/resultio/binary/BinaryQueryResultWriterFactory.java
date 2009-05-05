/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.resultio.binary;

import java.io.OutputStream;

import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;

/**
 * A {@link TupleQueryResultWriterFactory} for writers of binary query results.
 * 
 * @author Arjohn Kampman
 */
public class BinaryQueryResultWriterFactory implements TupleQueryResultWriterFactory {

	/**
	 * Returns {@link TupleQueryResultFormat#BINARY}.
	 */
	public TupleQueryResultFormat getFileFormat() {
		return TupleQueryResultFormat.BINARY;
	}

	/**
	 * Returns a new instance of BinaryQueryResultWriter.
	 */
	public TupleQueryResultWriter getWriter(OutputStream out) {
		return new BinaryQueryResultWriter(out);
	}
}
