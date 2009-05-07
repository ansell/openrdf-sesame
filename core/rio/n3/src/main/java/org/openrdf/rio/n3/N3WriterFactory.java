/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.n3;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for N3 writers.
 * 
 * @author Arjohn Kampman
 */
public class N3WriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#N3}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.N3;
	}

	/**
	 * Returns a new instance of {@link N3Writer}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new N3Writer(out);
	}

	/**
	 * Returns a new instance of {@link N3Writer}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new N3Writer(writer);
	}
}
