/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.representations;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.result.ModelResult;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ModelResultRepresentation extends OutputRepresentation {

	private static final int SMALL = 10;

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final ModelResult result;

	protected final RDFWriterFactory rdfWriterFactory;

	protected boolean trimNamespaces = false;

	public ModelResultRepresentation(ModelResult result, RDFWriterFactory rdfWriterFactory, MediaType mediaType)
	{
		super(mediaType);
		this.result = result;
		this.rdfWriterFactory = rdfWriterFactory;
	}

	/**
	 * Sets a flag controlling whether unused namespaces will be removed from the
	 * representation for small model results.
	 */
	public void setTrimNamespaces(boolean trimNamespaces) {
		this.trimNamespaces = trimNamespaces;
	}

	@Override
	public void release() {
		try {
			result.close();
		}
		catch (StoreException e) {
			logger.error("Failed to close model result", e);
		}
		finally {
			super.release();
		}
	}

	@Override
	public void write(OutputStream out)
		throws IOException
	{
		RDFWriter writer = rdfWriterFactory.getWriter(out);
		try {
			// writer.setBaseURI(req.getRequestURL().toString());
			writer.startRDF();

			Set<String> firstNamespaces = null;
			List<Statement> firstStatements = new ArrayList<Statement>(SMALL);

			// Only try to trim namespace if the RDF format supports namespaces
			// in the first place
			trimNamespaces = trimNamespaces && writer.getRDFFormat().supportsNamespaces();

			if (trimNamespaces) {
				// Gather the first few statements
				for (int i = 0; result.hasNext() && i < SMALL; i++) {
					firstStatements.add(result.next());
				}

				// Only trim namespaces if the set is small enough
				trimNamespaces = firstStatements.size() < SMALL;

				if (trimNamespaces) {
					// Gather the namespaces from the first few statements
					firstNamespaces = new HashSet<String>(SMALL);

					for (Statement st : firstStatements) {
						addNamespace(st.getSubject(), firstNamespaces);
						addNamespace(st.getPredicate(), firstNamespaces);
						addNamespace(st.getObject(), firstNamespaces);
						addNamespace(st.getContext(), firstNamespaces);
					}
				}
			}

			// Report namespace prefixes
			for (Map.Entry<String, String> ns : result.getNamespaces().entrySet()) {
				String prefix = ns.getKey();
				String namespace = ns.getValue();
				if (trimNamespaces == false || firstNamespaces.contains(namespace)) {
					writer.handleNamespace(prefix, namespace);
				}
			}

			// Report staements
			for (Statement st : firstStatements) {
				writer.handleStatement(st);
			}

			while (result.hasNext()) {
				Statement st = result.next();
				writer.handleStatement(st);
			}

			writer.endRDF();
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

	private void addNamespace(Value value, Set<String> namespaces) {
		if (value instanceof URI) {
			URI uri = (URI)value;
			namespaces.add(uri.getNamespace());
		}
	}
}
