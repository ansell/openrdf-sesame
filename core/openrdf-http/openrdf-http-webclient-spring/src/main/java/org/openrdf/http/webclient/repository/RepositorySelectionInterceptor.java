/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.webclient.server.Server;

public class RepositorySelectionInterceptor implements HandlerInterceptor {

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
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);

		if (request.getRequestURI().endsWith("/repository/overview.view")) {
			String id = request.getParameter("id");
			if (id != null) {
				Server server = (Server)session.getAttribute(Server.SERVER_KEY);
				repInfo = server.getRepositories().get(id);
				session.setAttribute(RepositoryInfo.REPOSITORY_KEY, repInfo);
			}
		}

		if (repInfo == null) {
			result = false;
			response.sendRedirect(request.getContextPath() + "/server/overview.view");
		}

		return result;
	}
}
