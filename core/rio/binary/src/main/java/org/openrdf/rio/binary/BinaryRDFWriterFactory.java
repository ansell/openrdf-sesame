/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.binary;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for Binary RDF writers.
 * 
 * @author Arjohn Kampman
 */
public class BinaryRDFWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#BINARY}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.BINARY;
	}

	/**
	 * Returns a new instance of {@link BinaryRDFWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new BinaryRDFWriter(out);
	}

	/**
	 * Returns a new instance of {@link BinaryRDFWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		throw new UnsupportedOperationException();
	}
}
