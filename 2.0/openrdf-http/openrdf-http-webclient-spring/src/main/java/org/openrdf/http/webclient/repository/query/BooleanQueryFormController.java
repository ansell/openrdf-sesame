/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPBooleanQuery;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.repository.http.HTTPRepository;

public class BooleanQueryFormController extends QueryFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		Map<String, Object> result = new HashMap<String, Object>();

		Map<String, QueryLanguage> queryLanguages = new TreeMap<String, QueryLanguage>();

		queryLanguages.put(QueryLanguage.SPARQL.getName().toUpperCase(), QueryLanguage.SPARQL);

		result.put("queryLanguages", queryLanguages);

		return result;
	}

	@Override
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception
	{
		ModelAndView result = null;

		HTTPRepository repo = (HTTPRepository)request.getSession().getAttribute(SessionKeys.REPOSITORY_KEY);

		BooleanQueryInfo qInfo = (BooleanQueryInfo)command;

		boolean queryResult = false;

		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();

			HTTPBooleanQuery query = (HTTPBooleanQuery)conn.prepareBooleanQuery(qInfo.getQueryLanguage(),
					qInfo.getQueryString());
			query.setIncludeInferred(qInfo.isIncludeInferred());
			try {
				queryResult = query.evaluate();
			}
			catch (HTTPQueryEvaluationException e) {
				if (e.isCausedByMalformedQueryException()) {
					throw e.getCauseAsMalformedQueryException();
				}
				else if (e.isCausedByRepositoryException()) {
					throw e.getCauseAsRepositoryException();
				}
				else if (e.isCausedByIOException()) {
					throw e.getCauseAsIOException();
				}
				else {
					throw e;
				}
			}
		}
		catch (RepositoryException e) {
			logger.info("Unable to process query", e);
			errors.reject("repository.error");
		}
		catch (MalformedQueryException e) {
			errors.rejectValue("queryString", "repository.query.error.malformed", new String[] { e.getMessage() }, "Malformed query");
		}
		catch (QueryEvaluationException e) {
			errors.reject("repository.query.error.evaluation");
		}
		finally {
			if (conn != null) {
				try {
					conn.close();
				}
				catch (RepositoryException e) {
					e.printStackTrace();
				}
			}
		}

		if (errors.hasErrors()) {
			result = showForm(request, response, errors, errors.getModel());
		}
		else {
			@SuppressWarnings("unchecked")
			Map<String, Object> model = errors.getModel();

			model.put("answer", queryResult);

			result = new ModelAndView(getSuccessView(), model);
		}

		return result;
	}
}
