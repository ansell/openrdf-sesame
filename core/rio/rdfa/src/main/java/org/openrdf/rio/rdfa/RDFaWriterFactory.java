/*
 * Copyright James Leigh (c) 2008.
 *
 * Licensed under the BSD license.
 */
package org.openrdf.rio.rdfa;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for RDFa writers.
 * 
 * @author James Leigh
 */
public class RDFaWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#RDFA}.
	 */
	public RDFFormat getFileFormat() {
		return RDFFormat.RDFA;
	}

	/**
	 * Returns a new instance of {@link PrettyRDFWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new PrettyRDFWriter(new RDFaMetaWriter(out));
	}

	/**
	 * Returns a new instance of {@link PrettyRDFWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new PrettyRDFWriter(new RDFaMetaWriter(writer));
	}
}
