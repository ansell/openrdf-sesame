/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import java.util.Date;

import org.restlet.data.Tag;

/**
 * @author Arjohn Kampman
 */
public class CacheInfo {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * A fixed prefix for the entity tags.
	 */
	private final String tagPrefix;

	/**
	 * The last-modified date.
	 */
	private Date lastModified;

	/**
	 * A counter that is increased with each update.
	 */
	private long version = 0L;

	/**
	 * A tag that can be added to served entities, changed with each update.
	 */
	private Tag entityTag;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public CacheInfo() {
		this(Long.toHexString(System.currentTimeMillis()));
	}

	public CacheInfo(String tagPrefix) {
		this.tagPrefix = tagPrefix;
		processUpdate();
	}

	public CacheInfo(CacheInfo parent) {
		// Use the parent's info until the first update is reported
		this.lastModified = parent.getLastModified();
		this.entityTag = parent.getEntityTag();
		
		this.tagPrefix = entityTag.getName();
	}

	/*---------*
	 * Methods *
	 *---------*/

	public synchronized void processUpdate() {
		lastModified = new Date();
		entityTag = new Tag(tagPrefix + "_" + Long.toHexString(++version));
	}

	public String getTagPrefix() {
		return tagPrefix;
	}

	public synchronized Date getLastModified() {
		return lastModified;
	}

	public synchronized Tag getEntityTag() {
		return entityTag;
	}
}
