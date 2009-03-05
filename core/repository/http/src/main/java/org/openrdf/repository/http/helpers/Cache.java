/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http.helpers;

/**
 * @author James Leigh
 */
public class Cache {

	private String eTag;

	private volatile long expires;

	public Cache(String eTag) {
		this.eTag = eTag;
	}

	public String getETag() {
		return eTag;
	}

	public boolean isFresh(long now) {
		if (expires == 0) {
			return false;
		}
		if (now < expires) {
			return true;
		}
		expires = 0;
		return false;
	}

	public void refreshed(long now, int maxAge) {
		if (maxAge > 0) {
			expires = now + maxAge * 1000;
		}
		else {
			stale();
		}
	}

	public void stale() {
		expires = 0;
	}

}
