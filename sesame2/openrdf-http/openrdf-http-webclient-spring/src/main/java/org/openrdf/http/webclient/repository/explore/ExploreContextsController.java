/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.explore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import info.aduna.iteration.CloseableIteration;
import info.aduna.text.ToStringComparator;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.model.Resource;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * 
 * @author Herko ter Horst
 */
public class ExploreContextsController implements Controller {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	private String view;

	public String getView() {
		return view;
	}

	public void setView(String view) {
		this.view = view;
	}

	public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) {
		ModelAndView result = new ModelAndView();
		result.setViewName(view);

		Map<String, Object> model = new HashMap<String, Object>();

		List<Resource> contexts = getContexts(request);
		Collections.sort(contexts, ToStringComparator.getInstance());
		model.put("contexts", contexts);

		result.addAllObjects(model);

		return result;
	}

	private List<Resource> getContexts(HttpServletRequest request) {
		List<Resource> result = null;

		HTTPRepository repo = (HTTPRepository)request.getSession().getAttribute(SessionKeys.REPOSITORY_KEY);

		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			CloseableIteration<? extends Resource, RepositoryException> contexts = conn.getContextIDs();
			result = new ArrayList<Resource>();
			while (contexts.hasNext()) {
				result.add(contexts.next());
			}
		}
		catch (RepositoryException e) {
			logger.warn("Unable to retrieve contexts", e);
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					logger.debug("Unable to close connection...", e);
				}
			}
		}

		return result;
	}

}
