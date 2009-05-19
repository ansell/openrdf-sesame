/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.restlet.Context;
import org.restlet.data.Conditions;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.data.Tag;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * Resource that (optinally) allows clients to cache its representations by
 * setting an entity tag and/or a last modified date.
 * 
 * @author Arjohn Kampman
 */
public abstract class CacheableResource extends Resource {

	public CacheableResource() {
		super();
	}

	public CacheableResource(Context context, Request request, Response response) {
		super(context, request, response);
	}

	/**
	 * Gets the resource's last-modified date, if available.
	 * 
	 * @return The last-modified date, or <tt>null</tt> if not available.
	 */
	protected abstract Date getLastModified();

	/**
	 * Gets the resource's entity tag, if available.
	 * 
	 * @return The entity tag, or <tt>null</tt> if not available.
	 */
	protected abstract Tag getEntityTag();

	/**
	 * Registers the specified variants and, if possible, sets last-modified
	 * dates and entity tags on these variants.
	 */
	protected void addCacheableVariants(Variant... variants) {
		addCacheableVariants(Arrays.asList(variants));
	}

	/**
	 * Registers the specified variants and, if possible, sets last-modified
	 * dates and entity tags on these variants.
	 */
	protected void addCacheableVariants(Iterable<? extends Variant> variants) {
		Date lastModified = getLastModified();
		Tag etag = getEntityTag();

		List<Variant> list = getVariants();

		for (Variant variant : variants) {
			// Note: method are moved to RepresentationInfo in restlet 2.0
			if (variant.getModificationDate() == null) {
				variant.setModificationDate(lastModified);
			}
			if (variant.getTag() == null) {
				variant.setTag(etag);
			}

			list.add(variant);
		}
	}

	@Override
	public final Representation getRepresentation(Variant variant) {
		// Resource.handleGet() only checks preconditions *after* the
		// Representation has been acquired. Prevent unnecessary Representation
		// creations by checking them here.
		Conditions conditions = getRequest().getConditions();

		if (conditions.hasSome()) {
			Representation representation = Representation.createEmpty();
			representation.setModificationDate(variant.getModificationDate());
			representation.setTag(variant.getTag());

			Status status = conditions.getStatus(getRequest().getMethod(), representation);

			if (status != null) {
				getResponse().setStatus(status);
				return null;
			}
		}

		Representation representation = super.getRepresentation(variant);

		// Set last-modified date and entity tag on the returned representation
		if (Status.SUCCESS_OK.equals(getResponse().getStatus()) && representation != null) {
			if (representation.getModificationDate() == null) {
				representation.setModificationDate(getLastModified());
			}
			if (representation.getTag() == null) {
				representation.setTag(getEntityTag());
			}
		}

		return representation;
	}
}
