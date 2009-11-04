/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.representations.TupleResultRepresentation;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.result.TupleResult;

/**
 * An abstract super class for resources that return tuple results.
 * 
 * @author Arjohn Kampman
 */
public abstract class TupleResultResource extends
		FileFormatResource<TupleQueryResultFormat, TupleQueryResultWriterFactory>
{

	public TupleResultResource() {
		super(TupleQueryResultWriterRegistry.getInstance());
	}

	protected final TupleQueryResultFormat getFileFormat(TupleQueryResultWriterFactory factory) {
		return factory.getTupleQueryResultFormat();
	}

	@Override
	protected final Representation getRepresentation(TupleQueryResultWriterFactory factory, MediaType mediaType)
		throws ResourceException
	{
		TupleResult tqr = getTupleResult();
		return new TupleResultRepresentation(tqr, factory, mediaType);
	}

	/**
	 * Returns the tuple result for this resource.
	 */
	public abstract TupleResult getTupleResult()
		throws ResourceException;
}
