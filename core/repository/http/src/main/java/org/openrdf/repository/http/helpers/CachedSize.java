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
public class CachedSize extends Cache {
	private long size;

	public CachedSize(long size, String eTag) {
		super(eTag);
		this.size = size;
	}

	public CachedSize(boolean present, String eTag) {
		super(eTag);
		this.size = present ? -1 : 0;
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

}
