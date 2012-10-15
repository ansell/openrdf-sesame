/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.nquads;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for N-Quads writers.
 * 
 * @author Peter Ansell
 */
public class NQuadsWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#NQUADS}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.NQUADS;
	}

	/**
	 * Returns a new instance of {@link NQuadsWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new NQuadsWriter(out);
	}

	/**
	 * Returns a new instance of {@link NQuadsWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new NQuadsWriter(writer);
	}
}
