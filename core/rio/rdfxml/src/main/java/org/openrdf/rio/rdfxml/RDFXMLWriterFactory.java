/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml;

import java.io.OutputStream;
import java.io.Writer;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * An {@link RDFWriterFactory} for RDF/XML writers.
 * 
 * @author Arjohn Kampman
 */
public class RDFXMLWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#RDFXML}.
	 */
	public RDFFormat getFileFormat() {
		return RDFFormat.RDFXML;
	}

	/**
	 * Returns a new instance of {@link RDFXMLWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new RDFXMLWriter(out);
	}

	/**
	 * Returns a new instance of {@link RDFXMLWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new RDFXMLWriter(writer);
	}
}
