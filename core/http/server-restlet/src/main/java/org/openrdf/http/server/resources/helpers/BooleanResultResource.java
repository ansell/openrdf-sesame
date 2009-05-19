/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.representations.BooleanResultRepresentation;
import org.openrdf.query.resultio.BooleanQueryResultFormat;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.result.BooleanResult;

/**
 * An abstract super class for resources that return tuple results.
 * 
 * @author Arjohn Kampman
 */
public abstract class BooleanResultResource extends
		FileFormatResource<BooleanQueryResultFormat, BooleanQueryResultWriterFactory>
{

	public BooleanResultResource(Context context, Request request, Response response) {
		super(BooleanQueryResultWriterRegistry.getInstance(), context, request, response);
	}

	protected final BooleanQueryResultFormat getFileFormat(BooleanQueryResultWriterFactory factory) {
		return factory.getBooleanQueryResultFormat();
	}

	@Override
	protected final Representation getRepresentation(BooleanQueryResultWriterFactory factory,
			MediaType mediaType)
		throws ResourceException
	{
		BooleanResult booleanResult = getBooleanResult();
		return new BooleanResultRepresentation(booleanResult, factory, mediaType);
	}

	/**
	 * Returns the boolean result for this resource.
	 */
	public abstract BooleanResult getBooleanResult()
		throws ResourceException;
}
