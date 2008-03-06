/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.manager;

import java.net.URL;
import java.text.Collator;

public class RepositoryInfo implements Comparable<RepositoryInfo> {

	private String id;

	private URL location;

	private String description;

	private boolean readable;

	private boolean writable;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public URL getLocation() {
		return location;
	}

	public void setLocation(URL location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isReadable() {
		return readable;
	}

	public void setReadable(boolean readable) {
		this.readable = readable;
	}

	public boolean isWritable() {
		return writable;
	}

	public void setWritable(boolean writable) {
		this.writable = writable;
	}

	public int compareTo(RepositoryInfo o) {
		int result = Collator.getInstance().compare(getDescription(), o.getDescription());
		if (result == 0) {
			result = getId().compareTo(o.getId());
		}
		return result;
	}
}
