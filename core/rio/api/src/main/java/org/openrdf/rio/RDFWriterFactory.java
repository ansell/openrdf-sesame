/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.io.OutputStream;
import java.io.Writer;

import info.aduna.lang.service.FileFormatService;

/**
 * A RDFWriterFactory returns {@link RDFWriter}s for a specific RDF format.
 * 
 * @author Arjohn Kampman
 */
public interface RDFWriterFactory extends FileFormatService<RDFFormat> {

	/**
	 * Returns an RDFWriter instance that will write to the supplied output
	 * stream.
	 * 
	 * @param out
	 *        The OutputStream to write the RDF to.
	 */
	public RDFWriter getWriter(OutputStream out);

	/**
	 * Returns an RDFWriter instance that will write to the supplied writer.
	 * 
	 * @param writer
	 *        The Writer to write the RDF to.
	 */
	public RDFWriter getWriter(Writer writer);
}
