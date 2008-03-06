/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import info.aduna.webapp.navigation.NavigationModel;

import org.openrdf.http.client.RepositoryInfo;
import org.openrdf.http.webclient.server.Server;

/**
 * @author Herko ter Horst
 */
public class NavigationRulesHandlerInterceptor implements HandlerInterceptor {

	public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
		throws Exception
	{
		// nop
	}

	public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
		throws Exception
	{
		// nop
	}

	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object arg2)
		throws Exception
	{
		HttpSession session = request.getSession(true);
		NavigationModel navigationModel = (NavigationModel)session.getAttribute(NavigationModel.NAVIGATION_MODEL_KEY);
		Server server = (Server)session.getAttribute(SessionKeys.SERVER_KEY);
		RepositoryInfo repoInfo = (RepositoryInfo)session.getAttribute(SessionKeys.REPOSITORY_INFO_KEY);

		navigationModel.getGroup("server").getView("overview").setEnabled(server != null);
		navigationModel.getGroup("repository").setEnabled(server != null && repoInfo != null);
		navigationModel.getGroup("repository").getGroup("modify").setEnabled(repoInfo != null && repoInfo.isWritable());
		navigationModel.getGroup("repository").getGroup("query").setEnabled(repoInfo != null && repoInfo.isReadable());
		navigationModel.getGroup("repository").getGroup("explore").setEnabled(repoInfo != null && repoInfo.isReadable());
		navigationModel.getGroup("repository").getGroup("extract").setEnabled(repoInfo != null && repoInfo.isReadable());

		return true;
	}

}
