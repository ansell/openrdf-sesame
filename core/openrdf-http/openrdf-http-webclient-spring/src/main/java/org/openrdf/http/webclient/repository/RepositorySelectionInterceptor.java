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

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.http.webclient.server.Server;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.repository.manager.RepositoryInfo;

public class RepositorySelectionInterceptor implements HandlerInterceptor {

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception e)
	{
		HttpSession session = request.getSession(true);
		session.removeAttribute(SessionKeys.REPOSITORY_EXCEPTION_KEY);
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView mav)
	{
		// do nothing
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception
	{
		boolean result = true;

		HttpSession session = request.getSession(true);
		RepositoryInfo repoInfo = (RepositoryInfo)session.getAttribute(SessionKeys.REPOSITORY_INFO_KEY);
		HTTPRepository repo = (HTTPRepository)session.getAttribute(SessionKeys.REPOSITORY_KEY);

		if (request.getRequestURI().endsWith("/repository/overview.view")) {
			String id = request.getParameter("id");
			if (id != null) {
				Server server = (Server)session.getAttribute(SessionKeys.SERVER_KEY);
				repo = (HTTPRepository)server.getRepositoryManager().getRepository(id);
				try {
					repo.initialize();
					repoInfo = server.getRepositoryManager().getRepositoryInfo(id);
				}
				catch (RepositoryException e) {
					session.setAttribute(SessionKeys.REPOSITORY_EXCEPTION_KEY, e);
				}
				session.setAttribute(SessionKeys.REPOSITORY_KEY, repo);
				session.setAttribute(SessionKeys.REPOSITORY_INFO_KEY, repoInfo);
			}
		}

		if (repo == null) {
			result = false;
			response.sendRedirect(request.getContextPath() + "/server/overview.view");
		}

		return result;
	}
}
