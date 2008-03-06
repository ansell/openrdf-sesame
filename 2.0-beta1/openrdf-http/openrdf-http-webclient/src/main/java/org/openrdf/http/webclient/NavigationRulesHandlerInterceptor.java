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

import org.openrdf.http.webclient.repository.RepositoryInfo;
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
		Server server = (Server)session.getAttribute(Server.SERVER_KEY);
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);

		navigationModel.getGroup("server").getView("overview").setEnabled(server != null);
		navigationModel.getGroup("repository").setEnabled(server != null && repInfo != null);
		navigationModel.getGroup("repository").getGroup("modify").setEnabled(repInfo != null && repInfo.isWritable());
		navigationModel.getGroup("repository").getGroup("query").setEnabled(repInfo != null && repInfo.isReadable());
		navigationModel.getGroup("repository").getGroup("explore").setEnabled(repInfo != null && repInfo.isReadable());
		navigationModel.getGroup("repository").getGroup("extract").setEnabled(repInfo != null && repInfo.isReadable());

		return true;
	}

}
