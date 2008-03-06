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
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.repository.RepositoryInfo;

public class ServerSelectionController extends SimpleFormController {

	static final Logger logger = LoggerFactory.getLogger(ServerSelectionController.class);

	static final String COOKIE_PREFIX = "server.select";

	static final String COOKIE_REMEMBER = "remember";

	static final String COOKIE_URL = "url";

	static final String COOKIE_TYPE = "type";

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception
	{
		ServerSelection serverSelection = (ServerSelection)command;

		String path = request.getContextPath();

		Cookie useAlwaysCookie = new Cookie(COOKIE_PREFIX + "." + COOKIE_REMEMBER,
				String.valueOf(serverSelection.isRemember()));
		useAlwaysCookie.setPath(path);
		useAlwaysCookie.setMaxAge(365 * 24 * 60 * 60);
		response.addCookie(useAlwaysCookie);

		Cookie serverURLCookie = new Cookie(COOKIE_PREFIX + "." + COOKIE_URL, serverSelection.getLocation());
		serverURLCookie.setPath(path);
		if (serverSelection.isRemember()) {
			serverURLCookie.setMaxAge(365 * 24 * 60 * 60);
		}
		response.addCookie(serverURLCookie);

		Cookie serverTypeCookie = new Cookie(COOKIE_PREFIX + "." + COOKIE_TYPE, serverSelection.getType());
		serverTypeCookie.setPath(path);
		if (serverSelection.isRemember()) {
			serverTypeCookie.setMaxAge(365 * 24 * 60 * 60);
		}
		response.addCookie(serverTypeCookie);

		HttpSession session = request.getSession(true);
		session.setAttribute(Server.SERVER_KEY, new Server(serverSelection.getLocation()));
		session.removeAttribute(RepositoryInfo.REPOSITORY_KEY);

		return super.onSubmit(request, response, null, errors);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
		throws Exception
	{
		Object result = super.formBackingObject(request);

		ServerSelection serverSelection = (ServerSelection)result;
		serverSelection.setDefaultServerURL(getDefaultServerUrl(request));

		setFromCookies(serverSelection, request.getCookies());

		return result;
	}

	static String getDefaultServerUrl(HttpServletRequest request) {
		StringBuilder result = new StringBuilder();

		try {
			URL requestURL = new URL(request.getRequestURL().toString());
			String protocol = requestURL.getProtocol();

			result.append(protocol);
			result.append("://");
			result.append(request.getServerName());
			if (!(protocol.equals("http") && request.getLocalPort() == 80)
					&& !(protocol.equals("https") && request.getLocalPort() == 443))
			{
				result.append(":");
				result.append(request.getLocalPort());
			}
			result.append("/openrdf-sesame/");
		}
		catch (MalformedURLException e) {
			// never happens
			e.printStackTrace();
		}

		return result.toString();
	}

	static void setFromCookies(ServerSelection serverSelection, Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				String cookieName = cookie.getName();
				String cookieValue = cookie.getValue();
				logger.debug("Found cookie '{}' = '{}'", cookieName, cookieValue);
				if (cookieName.startsWith(COOKIE_PREFIX)) {
					if (cookieName.endsWith(COOKIE_URL)) {
						serverSelection.setLocation(cookieValue);
					}
					else if (cookieName.endsWith(COOKIE_TYPE)) {
						serverSelection.setType(cookieValue);
					}
					else if (cookieName.endsWith(COOKIE_REMEMBER)) {
						serverSelection.setRemember(Boolean.parseBoolean(cookieValue));
					}
				}
			}
		}
		else {
			logger.debug("No cookies found in request");
		}
	}
}
