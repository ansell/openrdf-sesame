/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio;

import info.aduna.lang.service.FileFormatService;

/**
 * A RDFParserFactory returns {@link RDFParser}s for a specific RDF format.
 * 
 * @author Arjohn Kampman
 */
public interface RDFParserFactory extends FileFormatService<RDFFormat> {

	/**
	 * Returns a RDFParser instance.
	 */
	public RDFParser getParser();
}
