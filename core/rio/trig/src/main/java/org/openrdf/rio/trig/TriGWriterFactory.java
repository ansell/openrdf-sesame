/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.trig;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for TriG parsers.
 * 
 * @author Arjohn Kampman
 */
public class TriGWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#TRIG}.
	 */
	public RDFFormat getFileFormat() {
		return RDFFormat.TRIG;
	}

	/**
	 * Returns a new instance of {@link TriGWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new TriGWriter(out);
	}

	/**
	 * Returns a new instance of {@link TriGWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new TriGWriter(writer);
	}
}
