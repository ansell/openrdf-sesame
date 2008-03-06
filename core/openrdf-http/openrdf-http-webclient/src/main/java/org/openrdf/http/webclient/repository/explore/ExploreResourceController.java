/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.explore;

import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import info.aduna.iteration.Iterations;

import org.openrdf.http.webclient.properties.ResourcePropertyEditor;
import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryConnection;

/**
 * @author Herko ter Horst
 */
public class ExploreResourceController extends AbstractCommandController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String view;

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
		throws ServletException
	{
		HttpSession session = request.getSession();
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);

		binder.registerCustomEditor(Resource.class, new ResourcePropertyEditor(
				repInfo.getRepository().getValueFactory()));
	}

	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception
	{
		ModelAndView result = new ModelAndView();
		result.setViewName(getView());

		ExplorationResource exploration = (ExplorationResource)command;
		Resource resource = exploration.getResource();

		HttpSession session = request.getSession();
		RepositoryInfo repInfo = (RepositoryInfo)session.getAttribute(RepositoryInfo.REPOSITORY_KEY);

		@SuppressWarnings("unchecked")
		Map<String, Object> model = (Map<String, Object>)errors.getModel();
		model.put(getCommandName(), exploration);

		RepositoryConnection conn = null;
		try {
			conn = repInfo.getRepository().getConnection();
			model.put("asSubject", Iterations.asList(conn.getStatements(resource, null, null, true)));
			if (resource instanceof URI) {
				model.put("asPredicate", Iterations.asList(conn.getStatements(null, (URI)resource, null, true)));
			}
			model.put("asObject", Iterations.asList(conn.getStatements(null, null, resource, true)));

		}
		finally {
			if (conn != null) {
				conn.close();
			}
		}

		result.addAllObjects(model);

		return result;
	}
}