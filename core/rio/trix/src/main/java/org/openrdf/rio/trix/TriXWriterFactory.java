/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trix;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for TriX writers.
 * 
 * @author Arjohn Kampman
 */
public class TriXWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#TRIX}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.TRIX;
	}

	/**
	 * Returns a new instance of {@link TriXWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new TriXWriter(out);
	}

	/**
	 * Returns a new instance of {@link TriXWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new TriXWriter(writer);
	}
}
