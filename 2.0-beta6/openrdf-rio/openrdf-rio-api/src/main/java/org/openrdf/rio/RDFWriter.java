/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
}
