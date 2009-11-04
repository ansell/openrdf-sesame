/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.representations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Model;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;

/**
 * @author Arjohn Kampman
 */
public class ModelRepresentation extends OutputRepresentation {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final Model model;

	protected final RDFWriterFactory rdfWriterFactory;

	public ModelRepresentation(Model model, RDFWriterFactory rdfWriterFactory, MediaType mediaType) {
		super(mediaType);
		this.model = model;
		this.rdfWriterFactory = rdfWriterFactory;
	}

	@Override
	public void write(OutputStream out)
		throws IOException
	{
		try {
			RDFWriter rdfWriter = rdfWriterFactory.getWriter(out);
			// rdfWriter.setBaseURI(requestURL);

			rdfWriter.startRDF();

			for (Map.Entry<String, String> ns : model.getNamespaces().entrySet()) {
				rdfWriter.handleNamespace(ns.getKey(), ns.getValue());
			}

			for (Statement st : model) {
				rdfWriter.handleStatement(st);
			}

			rdfWriter.endRDF();
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
