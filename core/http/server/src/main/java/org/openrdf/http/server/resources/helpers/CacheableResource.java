/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.resources.helpers;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.restlet.data.MediaType;
import org.restlet.data.Tag;
import org.restlet.representation.RepresentationInfo;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;

import org.openrdf.http.server.helpers.CacheInfo;

/**
 * Resource that (optionally) allows clients to cache its representations by
 * setting an entity tag and/or a last modified date.
 * 
 * @author Arjohn Kampman
 */
public abstract class CacheableResource extends ServerResource {

	/**
	 * Gets the resource's cache info.
	 */
	protected abstract CacheInfo getCacheInfo();

	/**
	 * Registers the specified media types and, if possible, sets last-modified
	 * dates and entity tags on these variants.
	 */
	protected void addCacheableMediaTypes(MediaType... variants) {
		addCacheableMediaTypes(Arrays.asList(variants));
	}

	/**
	 * Registers the specified media types and, if possible, sets last-modified
	 * dates and entity tags on these variants.
	 */
	protected void addCacheableMediaTypes(Collection<MediaType> mediaTypes) {
		CacheInfo cacheInfo = getCacheInfo();

		if (cacheInfo != null) {
			Date lastModified = cacheInfo.getLastModified();
			Tag entityTag = cacheInfo.getEntityTag();

			for (MediaType mediaType : mediaTypes) {
				getVariants().add(new RepresentationInfo(mediaType, lastModified, entityTag));
			}
		}
		else {
			for (MediaType mediaType : mediaTypes) {
				getVariants().add(new Variant(mediaType));
			}
		}
	}
	// @Override
	// protected Representation get(Variant variant) {
	// Representation representation = super.get(variant);
	//
	// // Set last-modified date and entity tag on the returned representation
	// if (Status.SUCCESS_OK.equals(getResponse().getStatus()) && representation
	// != null) {
	// CacheInfo cacheInfo = getCacheInfo();
	//
	// if (cacheInfo != null) {
	// if (representation.getExpirationDate() == null) {
	// representation.setExpirationDate(new Date(System.currentTimeMillis() +
	// 10000));
	// }
	// }
	// }
	//
	// return representation;
	// }
}
