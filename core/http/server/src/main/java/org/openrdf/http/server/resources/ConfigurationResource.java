/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources;

import static org.openrdf.http.protocol.error.ErrorType.MALFORMED_DATA;
import static org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND;
import static org.restlet.data.Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE;

import java.io.IOException;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;

import org.openrdf.http.server.ErrorInfoException;
import org.openrdf.http.server.representations.ModelRepresentation;
import org.openrdf.http.server.resources.helpers.StatementResultResource;
import org.openrdf.model.Model;
import org.openrdf.model.impl.LinkedHashModel;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.Rio;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * @author Arjohn Kampman
 */
public class ConfigurationResource extends StatementResultResource {

	public static final String CONFIGURATION_ID_PARAM = "configurationID";

	private final String configurationID;

	public ConfigurationResource(Context context, Request request, Response response) {
		super(context, request, response);

		// Allow POST, PUT and DELETE
		this.setModifiable(true);

		configurationID = (String)request.getAttributes().get(CONFIGURATION_ID_PARAM);
	}

	protected final Representation getRepresentation(RDFWriterFactory factory, MediaType mediaType)
		throws ResourceException
	{
		try {
			Model config = getRepositoryManager().getRepositoryConfig(configurationID);

			if (config == null) {
				throw new ResourceException(CLIENT_ERROR_NOT_FOUND, "No such configuration: " + configurationID);
			}

			return new ModelRepresentation(config, factory, mediaType);
		}
		catch (StoreConfigException e) {
			throw new ResourceException(e);
		}
	}

	@Override
	protected String getFilenamePrefix() {
		return configurationID;
	}

	@Override
	public void storeRepresentation(Representation entity)
		throws ResourceException
	{
		Model model = parseContent(entity);

		try {
			getRepositoryManager().addRepositoryConfig(configurationID, model);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
		catch (StoreConfigException e) {
			throw new ResourceException(e);
		}
		// finally {
		// ConditionalRequestInterceptor.managerModified(request);
		// }
	}

	@Override
	public void removeRepresentations()
		throws ResourceException
	{
		// default to true, also assume the manager changed in case of error
		boolean configChanged = true;

		try {
			configChanged = getRepositoryManager().removeRepositoryConfig(configurationID);
		}
		catch (StoreException e) {
			throw new ResourceException(e);
		}
		catch (StoreConfigException e) {
			throw new ResourceException(e);
		}
		finally {
			if (configChanged) {
				// ConditionalRequestInterceptor.managerModified(request);
			}
		}

		if (!configChanged) {
			throw new ResourceException(CLIENT_ERROR_NOT_FOUND, "No such configuration: " + configurationID);
		}
	}

	private Model parseContent(Representation entity)
		throws ResourceException
	{
		String mimeType = entity.getMediaType().getName();
		RDFFormat rdfFormat = Rio.getParserFormatForMIMEType(mimeType);

		try {
			RDFParser parser = Rio.createParser(rdfFormat);

			Model model = new LinkedHashModel();
			parser.setRDFHandler(new StatementCollector(model));

			parser.parse(entity.getStream(), "");

			return model;
		}
		catch (UnsupportedRDFormatException e) {
			throw new ResourceException(CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE,
					"No RDF parser available for format " + rdfFormat.getName());
		}
		catch (RDFParseException e) {
			throw new ErrorInfoException(MALFORMED_DATA, e.getMessage());
		}
		catch (IOException e) {
			throw new ResourceException(e);
		}
		catch (RDFHandlerException e) {
			throw new ResourceException(e);
		}
	}
}
