/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.webclient.SessionKeys;

public class ServerSelectionInterceptor implements HandlerInterceptor {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

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
	
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception e)
		throws Exception
	{
		// do nothing
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView mav)
		throws Exception
	{
		// do nothing
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception
	{
		boolean result = true;

		HttpSession session = request.getSession(true);
		Server server = (Server)session.getAttribute(SessionKeys.SERVER_KEY);

		if (server == null) {
			logger.debug("No server found, attempting to set from cookie...");
			ServerSelection serverSelection = new ServerSelection();
			serverSelection.setDefaultServerContextName(getDefaultServerContextName());

			serverSelection.setDefaultServerURL(request);
			serverSelection.setFromCookies(request);
			
			if (serverSelection.isRemember()) {
				if (serverSelection.getLocation() != null) {
					logger.debug("Setting server from cookie: {}", serverSelection.getLocation());
					server = new Server(serverSelection.getLocation());
					session.setAttribute(SessionKeys.SERVER_KEY, server);
				}
			}
		}

		if (server == null) {
			logger.info("No server found, redirecting to selection form...");
			result = false;
			response.sendRedirect(request.getContextPath() + "/server/select.form");
		}

		return result;
	}
}
