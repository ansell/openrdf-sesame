/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.representations;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.data.MediaType;
import org.restlet.resource.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.result.GraphResult;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class GraphResultRepresentation extends OutputRepresentation {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final GraphResult graphResult;

	protected final RDFWriterFactory rdfWriterFactory;

	public GraphResultRepresentation(GraphResult graphResult, RDFWriterFactory rdfWriterFactory,
			MediaType mediaType)
	{
		super(mediaType);
		this.graphResult = graphResult;
		this.rdfWriterFactory = rdfWriterFactory;
	}

	@Override
	public void write(OutputStream out)
		throws IOException
	{
		try {
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
			QueryResultUtil.report(graphResult, rdfWriter);
		}
		catch (StoreException e) {
			logger.error("Query evaluation error", e);
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
		catch (RDFHandlerException e) {
			logger.error("Serialization error", e);
			throw new IOException("Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}
}
