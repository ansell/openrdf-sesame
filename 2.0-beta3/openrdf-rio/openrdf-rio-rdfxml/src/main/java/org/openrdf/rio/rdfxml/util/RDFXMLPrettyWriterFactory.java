/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.rdfxml.util;

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
public class RDFXMLPrettyWriterFactory implements RDFWriterFactory {

	/**
	 * Returns {@link RDFFormat#RDFXML}.
	 */
	public RDFFormat getRDFFormat() {
		return RDFFormat.RDFXML;
	}

	/**
	 * Returns a new instance of {@link RDFXMLPrettyWriter}.
	 */
	public RDFWriter getWriter(OutputStream out) {
		return new RDFXMLPrettyWriter(out);
	}

	/**
	 * Returns a new instance of {@link RDFXMLPrettyWriter}.
	 */
	public RDFWriter getWriter(Writer writer) {
		return new RDFXMLPrettyWriter(writer);
	}
}
