/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.ntriples;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for N-Triples writers.
 * 
 * @author Arjohn Kampman
 */
public class NTriplesWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#NTRIPLES}.
	 */
	public RDFFormat getFileFormat() {
		return RDFFormat.NTRIPLES;
	}

	/**
	 * Returns a new instance of {@link NTriplesWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new NTriplesWriter(out);
	}

	/**
	 * Returns a new instance of {@link NTriplesWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new NTriplesWriter(writer);
	}
}
