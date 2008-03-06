/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class ServerSelectionInterceptor implements HandlerInterceptor {

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
		Server server = (Server)session.getAttribute(Server.SERVER_KEY);

		if (server == null) {
			ServerSelection serverSelection = new ServerSelection();
			ServerSelectionController.setFromCookies(serverSelection, request.getCookies());
			if (serverSelection.isRemember()) {
				server = new Server(serverSelection.getLocation());
				session.setAttribute(Server.SERVER_KEY, server);
			}
			else {
				result = false;
				response.sendRedirect(request.getContextPath() + "/server/select.form");
			}
		}

		return result;
	}
}
