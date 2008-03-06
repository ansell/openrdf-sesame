/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import java.io.OutputStream;
import java.io.Writer;

/**
 * An interface for RDF document writers. To allow RDF document writers to be
 * created through reflection, all implementing classes should define at least
 * two public constructors: one with an {@link OutputStream} argument and one
 * with an {@link Writer} argument.
 */
public interface RDFWriter extends RDFHandler {

	/**
	 * Gets the RDF format that this RDFWriter uses.
	 */
	public RDFFormat getRDFFormat();

	/**
	 * Sets the output stream to which the RDF document should be written. Either
	 * this method or {@link #setWriter(Writer)} should be called before
	 * {@link RDFHandler#startRDF()} is called.
	 * 
	 * @param out
	 *        The output stream to write the RDF document to.
	 */
	public void setOutputStream(OutputStream out);

	/**
	 * Sets the writer to which the RDF document should be written. Either this
	 * method or {@link #setOutputStream(OutputStream))} should be called before
	 * {@link RDFHandler#startRDF()} is called.
	 * 
	 * @param writer
	 *        The writer to write the RDF document to.
	 */
	public void setWriter(Writer writer);
}
