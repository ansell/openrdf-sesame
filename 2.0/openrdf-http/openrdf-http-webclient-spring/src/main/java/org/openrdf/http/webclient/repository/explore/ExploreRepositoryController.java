/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
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
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import info.aduna.text.ToStringComparator;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.model.Resource;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPRepository;

/**
 * @author Herko ter Horst
 */
public class ExploreRepositoryController implements Controller {

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

		try {
			List<Resource> classes = getClasses(request);
			Collections.sort(classes, ToStringComparator.getInstance());
			model.put("classes", classes);
		}
		catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		result.addAllObjects(model);

		return result;
	}

	/**
	 * @param request
	 * @return
	 * @throws RepositoryException
	 * @throws MalformedQueryException
	 * @throws QueryEvaluationException
	 */
	private List<Resource> getClasses(HttpServletRequest request)
		throws RepositoryException, QueryEvaluationException, MalformedQueryException
	{
		List<Resource> result = new ArrayList<Resource>();

		HttpSession session = request.getSession();
		HTTPRepository repo = (HTTPRepository)session.getAttribute(SessionKeys.REPOSITORY_KEY);

		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();
			String query = "SELECT DISTINCT C FROM {} rdf:type {C}";
			TupleQueryResult classes = conn.prepareTupleQuery(QueryLanguage.SERQL, query).evaluate();
			try {
				while (classes.hasNext()) {
					BindingSet bindingSet = classes.next();
					Resource resource = (Resource)bindingSet.getValue("C");
					result.add(resource);
				}
			}
			finally {
				classes.close();
			}
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					logger.error("Unable to close connection", e);
				}
			}
		}

		return result;
	}
}
