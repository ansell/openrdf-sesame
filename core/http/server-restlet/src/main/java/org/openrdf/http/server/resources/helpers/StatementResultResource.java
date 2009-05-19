/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;

import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;

/**
 * An abstract super class for resources that return RDF statements.
 * 
 * @author Arjohn Kampman
 */
public abstract class StatementResultResource extends FileFormatResource<RDFFormat, RDFWriterFactory> {

	public StatementResultResource(Context context, Request request, Response response) {
		super(RDFWriterRegistry.getInstance(), context, request, response);
	}

	protected final RDFFormat getFileFormat(RDFWriterFactory factory) {
		return factory.getRDFFormat();
	}
}
