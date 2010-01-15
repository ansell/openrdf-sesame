/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2009-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.session;

import static org.openrdf.http.protocol.Protocol.SESSION_COOKIE;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.CookieSetting;

/**
 * @author Arjohn Kampman
 */
class SessionUtil {

	static String getSessionID(Request request) {
		Cookie sessionCookie = request.getCookies().getFirst(SESSION_COOKIE);
		if (sessionCookie != null) {
			return sessionCookie.getValue();
		}
		return null;
	}

	static void setSessionID(Response response, String value) {
		setSessionCookie(response, value, -1);
	}

	static void discardSessionID(Response response) {
		setSessionCookie(response, "", 0);
	}

	private static void setSessionCookie(Response response, String value, int maxAge) {
		CookieSetting cookie = new CookieSetting(SESSION_COOKIE, value);
		cookie.setPath(response.getRequest().getRootRef().getPath(true));
		cookie.setMaxAge(maxAge);
		response.getCookieSettings().add(cookie);
	}
}
