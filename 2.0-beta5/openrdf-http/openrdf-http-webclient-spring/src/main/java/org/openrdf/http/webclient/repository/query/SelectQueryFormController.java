/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.repository.http.HTTPTupleQuery;

public class SelectQueryFormController extends QueryFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws Exception
	{
		ModelAndView result = null;

		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);

		SelectQueryInfo qInfo = (SelectQueryInfo)command;

		TupleQueryResult queryResult = null;
		try {
			RepositoryConnection conn = repoInfo.getRepository().getConnection();

			HTTPTupleQuery query = (HTTPTupleQuery)conn.prepareTupleQuery(qInfo.getQueryLanguage(), qInfo.getQueryString());
			query.setIncludeInferred(qInfo.isIncludeInferred());
			try {
				queryResult = query.evaluate();
			}
			catch (HTTPQueryEvaluationException e) {
				if(e.isCausedByMalformedQueryException()) {
					throw e.getCauseAsMalformedQueryException();
				}
				else if(e.isCausedByRepositoryException()) {
					throw e.getCauseAsRepositoryException();
				}
				else if(e.isCausedByIOException()) {
					throw e.getCauseAsIOException();
				}
				else {
					throw e;
				}
			}	
		}
		catch (RepositoryException e) {
			errors.reject("repository.error");
		}
		catch (MalformedQueryException e) {
			Object[] args = getMalformedQueryMessageArguments(e.getLineNumber(), e.getColumnNumber(),
					e.getEncounteredToken(), e.getExpectedTokens());
			errors.rejectValue("queryString", "repository.query.error.malformed.parsed", args, "MALFORMED QUERY");
		}
		catch (QueryEvaluationException e) {
			errors.reject("repository.query.error.evaluation");
		}

		if (errors.hasErrors()) {
			result = showForm(request, response, errors, errors.getModel());
		}
		else {
			@SuppressWarnings("unchecked")
			Map<String, Object> model = errors.getModel();

			model.put("bindingNames", queryResult.getBindingNames());
			model.put("solutions", queryResult);

			result = new ModelAndView(getSuccessView(), model);
		}

		return result;
	}
}
