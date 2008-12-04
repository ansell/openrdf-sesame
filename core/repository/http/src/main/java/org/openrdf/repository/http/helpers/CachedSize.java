/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;


/**
 *
 * @author James Leigh
 */
public class CachedSize {
	private long size;

	private String eTag;

	private volatile long expires;

	public CachedSize(long size, String eTag) {
		super();
		this.size = size;
		this.eTag = eTag;
	}

	public CachedSize(boolean present, String eTag) {
		super();
		this.size = present ? -1 : 0;
		this.eTag = eTag;
	}

	public boolean isAbsent() {
		return size == 0;
	}


	public boolean isSizeAvailable() {
		return size != -1;
	}

	public Long getSize() {
		if (size == -1)
			return null;
		return size;
	}

	public String getETag() {
		return eTag;
	}

	public boolean isFresh(long now) {
		if (expires == 0)
			return false;
		if (now < expires)
			return true;
		expires = 0;
		return false;
	}

	public void refreshed(long now, int maxAge) {
		if (maxAge > 0) {
			expires = now + maxAge * 1000;
		} else {
			stale();
		}
	}

	public void stale() {
		expires = 0;
	}

}
