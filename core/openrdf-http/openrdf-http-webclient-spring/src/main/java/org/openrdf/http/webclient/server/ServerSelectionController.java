/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.repository.Repository;

public class ServerSelectionController extends SimpleFormController {

	static final Logger logger = LoggerFactory.getLogger(ServerSelectionController.class);

	
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

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception
	{
		ServerSelection serverSelection = (ServerSelection)command;

		String path = request.getContextPath();

		Cookie useAlwaysCookie = new Cookie(ServerSelection.COOKIE_PREFIX + "." + ServerSelection.COOKIE_REMEMBER,
				String.valueOf(serverSelection.isRemember()));
		useAlwaysCookie.setPath(path);
		useAlwaysCookie.setMaxAge(365 * 24 * 60 * 60);
		response.addCookie(useAlwaysCookie);

		Cookie serverURLCookie = new Cookie(ServerSelection.COOKIE_PREFIX + "." + ServerSelection.COOKIE_URL, serverSelection.getLocation());
		serverURLCookie.setPath(path);
		if (serverSelection.isRemember()) {
			serverURLCookie.setMaxAge(365 * 24 * 60 * 60);
		}
		response.addCookie(serverURLCookie);

		Cookie serverTypeCookie = new Cookie(ServerSelection.COOKIE_PREFIX + "." + ServerSelection.COOKIE_TYPE, serverSelection.getType());
		serverTypeCookie.setPath(path);
		if (serverSelection.isRemember()) {
			serverTypeCookie.setMaxAge(365 * 24 * 60 * 60);
		}
		response.addCookie(serverTypeCookie);

		HttpSession session = request.getSession(true);
		Server server = (Server)session.getAttribute(SessionKeys.SERVER_KEY);
		// if a new or different server was selected
		if (server == null || !server.getLocation().equals(serverSelection.getLocation())) {

			// TODO: verify shutting down the repos is the right thing to do here
			// (could/should queries still be running when switching to another
			// server in the same session?)

			// shutdown the "current" repository, if any
			Repository repo = (Repository)session.getAttribute(SessionKeys.REPOSITORY_KEY);
			if (repo != null) {
				repo.shutDown();
				session.removeAttribute(SessionKeys.REPOSITORY_KEY);
				session.removeAttribute(SessionKeys.REPOSITORY_INFO_KEY);
			}
			
			// insert the new Server into the session
			session.setAttribute(SessionKeys.SERVER_KEY, new Server(serverSelection.getLocation()));
		}

		return super.onSubmit(request, response, null, errors);
	}

	@Override
	protected Object formBackingObject(HttpServletRequest request)
		throws Exception
	{
		Object result = super.formBackingObject(request);

		ServerSelection serverSelection = (ServerSelection)result;
		serverSelection.setDefaultServerContextName(getDefaultServerContextName());
		serverSelection.setDefaultServerURL(request);
		serverSelection.setFromCookies(request);

		return result;
	}
}
