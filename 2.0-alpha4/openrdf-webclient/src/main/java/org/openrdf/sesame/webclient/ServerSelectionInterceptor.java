/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.sesame.webclient.server.SesameServer;

public class ServerSelectionInterceptor implements HandlerInterceptor {

	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			Exception e)
		throws Exception
	{
		// do nothing
	}

	public void postHandle(HttpServletRequest request, HttpServletResponse arg1, Object handler,
			ModelAndView mav)
		throws Exception
	{
		// do nothing
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
		throws Exception
	{
		boolean result = true;

		String redirectParam = request.getParameter("redirect");
		if (redirectParam == null || Boolean.parseBoolean(redirectParam)) {
			ServerSelection command = new ServerSelection();
			ServerSelectionController.setFromCookies(command, request.getCookies());
			if (command.isUseAlways()) {
				HttpSession session = request.getSession(true);
				session.setAttribute(SesameServer.SERVER_URL_KEY, command.getServerURL());

				response.sendRedirect("server.view");
			}
		}

		return result;
	}

}
