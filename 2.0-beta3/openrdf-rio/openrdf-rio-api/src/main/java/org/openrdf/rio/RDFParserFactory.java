/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

/**
 * A RDFParserFactory returns {@link RDFParser}s for a specific RDF format.
 * 
 * @author Arjohn Kampman
 */
public interface RDFParserFactory {

	/**
	 * Returns the RDF format for this factory.
	 */
	public RDFFormat getRDFFormat();

	/**
	 * Returns a RDFParser instance.
	 */
	public RDFParser getParser();
}
