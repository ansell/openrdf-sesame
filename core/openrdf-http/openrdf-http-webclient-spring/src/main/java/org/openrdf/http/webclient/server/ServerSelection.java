/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public class ServerSelection {

	public static final String DEFAULT = "default";

	public static final String OTHER = "other";

	static final String COOKIE_PREFIX = "server.select";

	static final String COOKIE_REMEMBER = "remember";

	static final String COOKIE_URL = "url";

	static final String COOKIE_TYPE = "type";
	
	private String type = DEFAULT;

	private String location;

	private String defaultServerURL;

	private boolean remember;
	
	private String defaultServerContextName;

	/**
	 * @return Returns the defaultWebapp.
	 */
	public String getDefaultServerContextName() {
		return defaultServerContextName;
	}

	/**
	 * @param defaultWebapp The defaultWebapp to set.
	 */
	public void setDefaultServerContextName(String defaultWebapp) {
		this.defaultServerContextName = defaultWebapp;
	}

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
	
	void setDefaultServerURL(HttpServletRequest request) {
		StringBuilder result = new StringBuilder();

		try {
			URL requestURL = new URL(request.getRequestURL().toString());
			String protocol = requestURL.getProtocol();

			result.append(protocol);
			result.append("://");
			result.append(request.getServerName());
			
			// append port if different from default for protocol
			if (!(protocol.equals("http") && request.getLocalPort() == 80)
					&& !(protocol.equals("https") && request.getLocalPort() == 443))
			{
				result.append(":");
				result.append(request.getLocalPort());
			}
			result.append(getDefaultServerContextName());
		}
		catch (MalformedURLException e) {
			// never happens
			e.printStackTrace();
		}

		setDefaultServerURL(result.toString());
	}
	
	void setFromCookies(HttpServletRequest request) {
		Cookie[] cookies =  request.getCookies();
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				String cookieValue = cookie.getValue();
				if (cookieName.startsWith(COOKIE_PREFIX)) {
					if (cookieName.endsWith(COOKIE_URL)) {
						setLocation(cookieValue);
					}
					else if (cookieName.endsWith(COOKIE_TYPE)) {
						setType(cookieValue);
					}
					else if (cookieName.endsWith(COOKIE_REMEMBER)) {
						setRemember(Boolean.parseBoolean(cookieValue));
					}
				}
			}
		}
	}
}
