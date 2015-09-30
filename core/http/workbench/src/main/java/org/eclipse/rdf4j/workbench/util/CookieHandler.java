/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.workbench.util;

import static java.lang.Integer.parseInt;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.rdf4j.workbench.base.TransformationServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles cookies for TransformationServlet.
 * 
 * @author Dale Visser
 */
public class CookieHandler {

	private static final String COOKIE_AGE_PARAM = "cookie-max-age";

	private static final Logger LOGGER = LoggerFactory.getLogger(CookieHandler.class);

	private final ServletConfig config;

	private final TransformationServlet servlet;

	public CookieHandler(final ServletConfig config, final TransformationServlet servlet) {
		this.config = config;
		this.servlet = servlet;
	}

	public void updateCookies(final WorkbenchRequest req, final HttpServletResponse resp)
		throws UnsupportedEncodingException
	{
		for (String name : this.servlet.getCookieNames()) {
			if (req.isParameterPresent(name)) {
				addCookie(req, resp, name);
			}
		}
	}

	private void addCookie(final WorkbenchRequest req, final HttpServletResponse resp, final String name)
		throws UnsupportedEncodingException
	{
		final String raw = req.getParameter(name);
		final String value = URLEncoder.encode(raw, "UTF-8");
		LOGGER.info("name: {}\nvalue: {}", name, value);
		LOGGER.info("un-encoded value: {}\n--", raw);
		final Cookie cookie = new Cookie(name, value);
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
					// Cookie already exists. Tell the browser we are using it.
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
	public void addTotalResultCountCookie(WorkbenchRequest req, HttpServletResponse resp,
			int value)
	{
		addCookie(req, resp, "total_result_count", String.valueOf(value));
	}
	
	public void addCookie(WorkbenchRequest req, HttpServletResponse resp,
			String name, String value){
		final Cookie cookie = new Cookie(name, value);
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
