/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

public class ServerSelection {

	public static final String DEFAULT = "default";

	public static final String OTHER = "other";

	private String type = DEFAULT;

	private String location;

	private String defaultServerURL;

	private boolean remember;

	public String getLocation() {
		String result = location;

		if (getType().equals(DEFAULT)) {
			result = defaultServerURL;
		}
		return result;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isRemember() {
		return remember;
	}

	public void setRemember(boolean remember) {
		this.remember = remember;
	}

	public String getDefaultServerURL() {
		return defaultServerURL;
	}

	public void setDefaultServerURL(String localURL) {
		this.defaultServerURL = localURL;
	}
}
