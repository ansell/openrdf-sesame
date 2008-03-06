/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.webclient.repository.query;

import java.io.ByteArrayOutputStream;
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

import org.openrdf.http.webclient.SessionKeys;
import org.openrdf.http.webclient.properties.RDFFormatPropertyEditor;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.resultio.QueryResultIO;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.http.HTTPGraphQuery;
import org.openrdf.repository.http.HTTPQueryEvaluationException;
import org.openrdf.repository.http.HTTPRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriterFactory;
import org.openrdf.rio.RDFWriterRegistry;
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
		throws Exception
	{
		ModelAndView result = null;

		HTTPRepository repo = (HTTPRepository)request.getSession().getAttribute(SessionKeys.REPOSITORY_KEY);

		ConstructQueryInfo qInfo = (ConstructQueryInfo)command;

		String resultString = "";

		RepositoryConnection conn = null;
		try {
			conn = repo.getConnection();

			HTTPGraphQuery query = (HTTPGraphQuery)conn.prepareGraphQuery(qInfo.getQueryLanguage(),
					qInfo.getQueryString());
			query.setIncludeInferred(qInfo.isIncludeInferred());
			GraphQueryResult queryResult = null;
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

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				QueryResultIO.write(queryResult, qInfo.getResultFormat(), out);
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
		}
		catch (RepositoryException e) {
			errors.reject("repository.error");
		}
		catch (MalformedQueryException e) {
			errors.rejectValue("queryString", "repository.query.error.malformed", new String[] { e.getMessage() }, "Malformed query");
		}
		catch (QueryEvaluationException e) {
			errors.reject("repository.query.error.evaluation");
		}
		finally {
			// FIXME: check to see where this connection gets closed
			// if (conn != null) {
			// try {
			// conn.close();
			// }
			// catch (RepositoryException e) {
			// e.printStackTrace();
			// }
			// }
		}

		if (errors.hasErrors()) {
			result = showForm(request, response, errors, errors.getModel());
		}
		else {
			@SuppressWarnings("unchecked")
			Map<String, Object> model = errors.getModel();

			model.put("queryinfo", qInfo);
			model.put("queryresult", resultString);

			result = new ModelAndView(getSuccessView(), model);
		}

		return result;
	}

	@Override
	protected Map<String, Object> referenceData(HttpServletRequest request) {
		@SuppressWarnings("unchecked")
		Map<String, Object> result = (Map<String, Object>)super.referenceData(request);

		Map<String, String> resultFormats = new TreeMap<String, String>();

		for (RDFWriterFactory factory : RDFWriterRegistry.getInstance().getAll()) {
			RDFFormat resultFormat = factory.getRDFFormat();
			resultFormats.put(resultFormat.getName(), resultFormat.getName());
		}

		result.put("resultFormats", resultFormats);

		return result;
	}
}
