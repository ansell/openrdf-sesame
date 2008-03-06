/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient;

public class ServerSelection {

	public static final String LOCAL = "local";

	public static final String REMOTE = "remote";

	private String type = LOCAL;

	private String serverURL;
	
	private String localServerURL;

	private boolean useAlways;

	public String getServerURL() {
		return serverURL;
	}

	public void setServerURL(String serverURL) {
		this.serverURL = serverURL;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public boolean isUseAlways() {
		return useAlways;
	}

	public void setUseAlways(boolean useAlways) {
		this.useAlways = useAlways;
	}
	
	public String getLocalServerURL() {
		return localServerURL;
	}
	
	public void setLocalServerURL(String localURL) {
		this.localServerURL = localURL;
		if(getType().equals(LOCAL)) {
			setServerURL(localURL);
		}
	}
}
