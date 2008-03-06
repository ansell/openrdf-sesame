/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.servlet.ModelAndView;

import org.openrdf.http.webclient.properties.RDFFormatPropertyEditor;
import org.openrdf.http.webclient.repository.RepositoryInfo;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.resultio.QueryResultUtil;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.UnsupportedRDFormatException;

public class ConstructQueryFormController extends QueryFormController {

	final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder)
		throws ServletException
	{
		binder.registerCustomEditor(RDFFormat.class, new RDFFormatPropertyEditor());
	}

	@Override
	public ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command,
			BindException errors)
		throws ServletException
	{
		RepositoryInfo repoInfo = (RepositoryInfo)request.getSession().getAttribute(
				RepositoryInfo.REPOSITORY_KEY);

		ConstructQueryInfo qInfo = (ConstructQueryInfo)command;

		String resultString = "";

		try {
			RepositoryConnection conn = repoInfo.getRepository().getConnection();

			GraphQuery query = conn.prepareGraphQuery(qInfo.getQueryLanguage(), qInfo.getQuery());
			query.setIncludeInferred(qInfo.isIncludeInferred());
			GraphQueryResult result = query.evaluate();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				QueryResultUtil.write(result, qInfo.getResultFormat(), out);
				resultString = new String(out.toByteArray(), qInfo.getResultFormat().getCharset().name());
			}
			catch (RDFHandlerException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (UnsupportedRDFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
		model.put("queryresult", resultString);

		return new ModelAndView(getSuccessView(), model);
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request)
	{
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>)super.referenceData(request);

		Map<String, String> resultFormats = new TreeMap<String, String>();
		for (RDFFormat resultFormat : RDFFormat.values()) {
			resultFormats.put(resultFormat.getName(), resultFormat.getName());
		}

		result.put("resultFormats", resultFormats);

		return result;
	}

}
