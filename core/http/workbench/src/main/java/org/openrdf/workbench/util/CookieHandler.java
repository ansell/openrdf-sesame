/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2012.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.workbench.util;

import static java.lang.Integer.parseInt;

import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.openrdf.workbench.base.TransformationServlet;

/**
 * Handles cookies for TransformationServlet.
 * 
 * @author Dale Visser
 */
public class CookieHandler {

	private static final String COOKIE_AGE_PARAM = "cookie-max-age";

	private final ServletConfig config;

	private final TransformationServlet servlet;
	
	public CookieHandler(final ServletConfig config, final TransformationServlet servlet) {
		this.config = config;
		this.servlet = servlet;
	}

	public void updateCookies(final WorkbenchRequest req, final HttpServletResponse resp) {
		for (String name : this.servlet.getCookieNames()) {
			if (req.isParameterPresent(name)) {
				addCookie(req, resp, name);
			}
		}
	}

	private void addCookie(final WorkbenchRequest req, final HttpServletResponse resp, final String name) {
		final Cookie cookie = new Cookie(name, req.getParameter(name));
		if (null == req.getContextPath()) {
			cookie.setPath("/");
		}
		else {
			cookie.setPath(req.getContextPath());
		}
		cookie.setMaxAge(parseInt(config.getInitParameter(COOKIE_AGE_PARAM)));
		addCookie(req, resp, cookie);
	}

	private void addCookie(final WorkbenchRequest req, final HttpServletResponse resp, final Cookie cookie) {
		final Cookie[] cookies = req.getCookies();
		if (cookies != null) {
			for (Cookie c : cookies) {
				if (cookie.getName().equals(c.getName()) && cookie.getValue().equals(c.getValue())) {
					// cookie already exists
					// tell the browser we are using it
					resp.addHeader("Vary", "Cookie");
				}
			}
		}
		resp.addCookie(cookie);
	}

	/**
	 * Add a 'total_result_count' cookie. Used by both QueryServlet and
	 * ExploreServlet.
	 * 
	 * @param req
	 *        the request object
	 * @param resp
	 *        the response object
	 * @value the value to give the cookie
	 */
	public void addTotalResultCountCookie(final WorkbenchRequest req, final HttpServletResponse resp,
			final int value)
	{
		final Cookie cookie = new Cookie("total_result_count", String.valueOf(value));
		if (null == req.getContextPath()) {
			cookie.setPath("/");
		}
		else {
			cookie.setPath(req.getContextPath());
		}
		cookie.setMaxAge(Integer.parseInt(config.getInitParameter(COOKIE_AGE_PARAM)));
		this.addCookie(req, resp, cookie);
	}
}
