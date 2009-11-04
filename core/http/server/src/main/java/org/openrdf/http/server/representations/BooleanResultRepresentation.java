/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.representations;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.query.resultio.BooleanQueryResultWriter;
import org.openrdf.query.resultio.BooleanQueryResultWriterFactory;
import org.openrdf.result.BooleanResult;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class BooleanResultRepresentation extends OutputRepresentation {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final BooleanResult booleanResult;

	protected final BooleanQueryResultWriterFactory factory;

	public BooleanResultRepresentation(BooleanResult booleanResult, BooleanQueryResultWriterFactory factory,
			MediaType mediaType)
	{
		super(mediaType);
		this.booleanResult = booleanResult;
		this.factory = factory;
	}

	@Override
	public void write(OutputStream out)
		throws IOException
	{
		try {
			BooleanQueryResultWriter qrWriter = factory.getWriter(out);
			qrWriter.write(booleanResult.asBoolean());
		}
		catch (StoreException e) {
			logger.error("Query evaluation error", e);
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}
}
