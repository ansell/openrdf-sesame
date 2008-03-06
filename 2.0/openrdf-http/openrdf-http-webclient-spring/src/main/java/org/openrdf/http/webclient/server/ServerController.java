/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.server;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import info.aduna.webapp.Message;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.repository.RepositoryException;

public class ServerController implements Controller {

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
		Server server = (Server)request.getSession().getAttribute(SessionKeys.SERVER_KEY);

		Map<String, Object> model = new HashMap<String, Object>();
		try {
			model.put("repositoryInfos", server.getRepositoryInfos());
		}
		catch (RepositoryException e) {
			model.put("repositoryInfos", Collections.emptyList());
			model.put("message", new Message(Message.Type.WARN, "server.overview.repositoryInfoError"));
		}

		model.put("location", server.getLocation());

		return new ModelAndView("server/overview", model);
	}
}
