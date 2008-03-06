/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import org.openrdf.http.client.RepositoryInfo;
import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.repository.http.HTTPRepository;

public class RepositoryController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
		HttpSession session = request.getSession(false);
		
		HTTPRepository repo = (HTTPRepository)session.getAttribute(SessionKeys.REPOSITORY_KEY);
		RepositoryInfo repoInfo = (RepositoryInfo)session.getAttribute(SessionKeys.REPOSITORY_INFO_KEY);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("repository", repo);
		model.put("repositoryInfo", repoInfo);

		return new ModelAndView("repository/overview", model);
	}

}
