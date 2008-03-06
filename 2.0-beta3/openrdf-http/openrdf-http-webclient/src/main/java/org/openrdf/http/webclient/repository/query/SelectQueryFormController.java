/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

public class SelectQueryFormController extends QueryFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws ServletException
	{
		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);

		SelectQueryInfo qInfo = (SelectQueryInfo)command;

		TupleQueryResult result = null;
		try {
			RepositoryConnection conn = repoInfo.getRepository().getConnection();

			TupleQuery query = conn.prepareTupleQuery(qInfo.getQueryLanguage(), qInfo.getQuery());
			query.setIncludeInferred(qInfo.isIncludeInferred());
			result = query.evaluate();
		}
		catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (MalformedQueryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (QueryEvaluationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Map<String, Object> model = new HashMap<String, Object>();
		model.put("queryinfo", qInfo);
		model.put("bindingNames", result.getBindingNames());
		model.put("solutions", result);

		return new ModelAndView(getSuccessView(), model);
	}
}
