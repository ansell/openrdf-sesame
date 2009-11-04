/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import static org.restlet.data.Status.CLIENT_ERROR_NOT_ACCEPTABLE;

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;

import info.aduna.lang.FileFormat;
import info.aduna.lang.service.FileFormatServiceRegistry;

import org.openrdf.http.server.helpers.ServerUtil;

/**
 * Abstract super class for resources that use a
 * {@link FileFormatServiceRegistry} to write representations of their resource.
 * This class takes care of the required content negotiation.
 * 
 * @author Arjohn Kampman
 */
public abstract class FileFormatResource<FF extends FileFormat, S> extends SesameResource {

	protected final FileFormatServiceRegistry<FF, S> registry;

	public FileFormatResource(FileFormatServiceRegistry<FF, S> registry) {
		this.registry = registry;
	}

	@Override
	protected void doInit() {
		super.doInit();

		// Add available file formats
		List<MediaType> mediaTypes = new ArrayList<MediaType>();

		for (FileFormat format : registry.getKeys()) {
			for (String mimeType : format.getMIMETypes()) {
				mediaTypes.add(new MediaType(mimeType));
			}
		}

		addCacheableMediaTypes(mediaTypes);
	}

	@Override
	protected final Representation get(Variant variant)
		throws ResourceException
	{
		getResponse().setDimensions(ServerUtil.VARY_ACCEPT);

		MediaType mediaType = variant.getMediaType();

		FF format = registry.getFileFormatForMIMEType(mediaType.getName());

		if (format == null) {
			throw new ResourceException(CLIENT_ERROR_NOT_ACCEPTABLE, "No acceptable file format found.");
		}

		S service = registry.get(format);

		Representation representation = getRepresentation(service, mediaType);

		String filenamePrefix = getFilenamePrefix();
		if (filenamePrefix != null) {
			FF fileFormat = getFileFormat(service);
			representation.setDownloadName(filenamePrefix + "." + fileFormat.getDefaultFileExtension());
		}

		return representation;
	}

	protected abstract FF getFileFormat(S service);

	protected abstract Representation getRepresentation(S service, MediaType mediaType)
		throws ResourceException;

	/**
	 * Gets the prefix for the filename that is sent to the client. The default
	 * file extension for the {@link #getFileFormat(Object) returned file format}
	 * is appended to this prefix to form the appropriate file name. No file name
	 * will be sent to the client if this method returns <tt>null</tt>, which is
	 * the default.
	 */
	protected String getFilenamePrefix() {
		return null;
	}
}
