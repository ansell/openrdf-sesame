/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;

/**
 * An abstract super class for resources that return RDF statements.
 * 
 * @author Arjohn Kampman
 */
public abstract class StatementResultResource extends FileFormatResource<RDFFormat, RDFWriterFactory> {

	public StatementResultResource() {
		super(RDFWriterRegistry.getInstance());
	}

	protected final RDFFormat getFileFormat(RDFWriterFactory factory) {
		return factory.getRDFFormat();
	}
}
