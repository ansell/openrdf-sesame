/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class RepositoryController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		HttpSession session = request.getSession(false);
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);
				
		return new ModelAndView("repository/overview", "repository", repInfo);
	}

}
