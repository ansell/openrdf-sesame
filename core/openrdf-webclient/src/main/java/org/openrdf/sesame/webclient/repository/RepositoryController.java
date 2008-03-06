/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sesame.webclient.repository;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

public class RepositoryController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response)
		throws ServletException, IOException
	{
		String location = request.getParameter("location");
		String description = request.getParameter("description");
		
		RepositoryInfo repInfo = new RepositoryInfo();
		repInfo.setLocation(location);
		repInfo.setDescription(description);
		
		return new ModelAndView("repository/overview", "repository", repInfo);
	}

}
