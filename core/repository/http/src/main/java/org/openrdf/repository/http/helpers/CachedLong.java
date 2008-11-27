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
public class CachedLong {
	private long value;

	private String eTag;

	private long expires;

	public CachedLong(long value, String eTag, int maxAge) {
		super();
		this.value = value;
		this.eTag = eTag;
		if (maxAge > 0) {
			this.expires = System.currentTimeMillis() + maxAge * 1000;
		}
	}

	public long getValue() {
		return value;
	}

	public String getETag() {
		return eTag;
	}

	public boolean isFresh() {
		return expires != 0 && System.currentTimeMillis() < expires;
	}

	public void stale() {
		expires = 0;
	}

}
