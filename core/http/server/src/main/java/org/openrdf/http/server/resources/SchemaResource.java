/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.representations.ModelRepresentation;
import org.openrdf.http.server.resources.helpers.StatementResultResource;
import org.openrdf.model.Model;
import org.openrdf.model.util.ModelOrganizer;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.store.StoreConfigException;

/**
 * @author Arjohn Kampman
 */
public class SchemaResource extends StatementResultResource {

	protected final Representation getRepresentation(RDFWriterFactory factory, MediaType mediaType)
		throws ResourceException
	{
		try {
			Model schemas = getRepositoryManager().getConfigTemplateManager().getSchemas();
			schemas = new ModelOrganizer(schemas).organize();
			return new ModelRepresentation(schemas, factory, mediaType);
		}
		catch (StoreConfigException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return "schemas";
	}
}
