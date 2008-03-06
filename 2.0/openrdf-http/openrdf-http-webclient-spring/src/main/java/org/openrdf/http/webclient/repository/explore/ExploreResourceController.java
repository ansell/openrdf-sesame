/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.explore;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.http.webclient.properties.ResourcePropertyEditor;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

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
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
		HttpSession session = request.getSession();
		HTTPRepository repo = (HTTPRepository)session.getAttribute(SessionKeys.REPOSITORY_KEY);

		binder.registerCustomEditor(Resource.class, new ResourcePropertyEditor(repo.getValueFactory()));
	}

	@Override
	protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception
	{
		ExplorationResource exploration = (ExplorationResource)command;
		Resource resource = exploration.getResource();

		@SuppressWarnings("unchecked")
		Map<String, Object> model = (Map<String, Object>)errors.getModel();
		model.put(getCommandName(), exploration);

		if (resource != null) {
			HttpSession session = request.getSession();
			Repository repo = (Repository)session.getAttribute(SessionKeys.REPOSITORY_KEY);

			try {
				RepositoryConnection con = repo.getConnection();
				try {
					model.put("asSubject", con.getStatements(resource, null, null, true).asList());

					if (resource instanceof URI) {
						model.put("asPredicate", con.getStatements(null, (URI)resource, null, true).asList());
					}

					model.put("asObject", con.getStatements(null, null, resource, true).asList());
				}
				finally {
					con.close();
				}
			}
			catch (RepositoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return new ModelAndView(getView(), model);
	}
}