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

import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.TupleQueryResultWriterFactory;
import org.openrdf.result.TupleResult;
import org.openrdf.result.util.QueryResultUtil;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class TupleResultRepresentation extends OutputRepresentation {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final TupleResult tupleResult;

	protected final TupleQueryResultWriterFactory factory;

	public TupleResultRepresentation(TupleResult tupleResult, TupleQueryResultWriterFactory factory,
			MediaType mediaType)
	{
		super(mediaType);
		this.tupleResult = tupleResult;
		this.factory = factory;
	}

	@Override
	public void write(OutputStream out)
		throws IOException
	{
		try {
			TupleQueryResultWriter qrWriter = factory.getWriter(out);
			QueryResultUtil.report(tupleResult, qrWriter);
		}
		catch (StoreException e) {
			logger.error("Query evaluation error", e);
			throw new IOException("Query evaluation error: " + e.getMessage());
		}
		catch (TupleQueryResultHandlerException e) {
			logger.error("Serialization error", e);
			throw new IOException("Serialization error: " + e.getMessage());
		}
		finally {
			out.close();
		}
	}
}
