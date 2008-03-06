/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.mvc.AbstractController;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.ClientRequestException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.model.Value;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

/**
 * Handles queries on a repository and renders the results in a format suitable
 * to the type of query.
 * 
 * @author Herko ter Horst
 */
public class RepositoryController extends AbstractController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	public RepositoryController()
		throws ApplicationContextException
	{
		setSupportedMethods(new String[] { METHOD_GET, METHOD_POST });
	}

	@Override
	protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response)
		throws Exception
	{
		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String reqMethod = request.getMethod();
		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET query");
		}
		else if (METHOD_POST.equals(reqMethod)) {
			logger.info("POST query");
			String contentType = request.getContentType();

			if (!Protocol.FORM_MIME_TYPE.equals(contentType)) {
				throw new ClientRequestException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported content type: "
						+ contentType);
			}
		}

		View view = null;
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(QueryResultView.FILENAME_HINT_KEY, "query-result");

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		if (queryStr != null) {
			Query query = getQuery(repository, repositoryCon, queryStr, request, response);

			if (query instanceof TupleQuery) {
				TupleQuery tQuery = (TupleQuery)query;
				model.put(QueryResultView.QUERY_RESULT_KEY, tQuery.evaluate());
				view = TupleQueryResultView.getInstance();
			}
			else if (query instanceof GraphQuery) {
				GraphQuery gQuery = (GraphQuery)query;
				model.put(QueryResultView.QUERY_RESULT_KEY, gQuery.evaluate());
				view = GraphQueryResultView.getInstance();
			}
			else {
				throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query type: "
						+ query.getClass().getName());
			}
		}
		else {
			throw new ClientRequestException(SC_BAD_REQUEST, "Missing parameter: " + QUERY_PARAM_NAME);
		}

		return new ModelAndView(view, model);
	}

	private Query getQuery(Repository repository, RepositoryConnection repositoryCon, String queryStr,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientRequestException
	{
		Query result = null;

		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr.toUpperCase());

			if (queryLn == null) {
				throw new ClientRequestException(SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
			}
		}

		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		try {
			result = repositoryCon.prepareQuery(queryLn, queryStr);
			result.setIncludeInferred(includeInferred);

			// determine if any variable bindings have been set on this query.
			@SuppressWarnings("unchecked")
			Enumeration<String> parameterNames = (Enumeration<String>)request.getParameterNames();

			while (parameterNames.hasMoreElements()) {
				String parameterName = (String)parameterNames.nextElement();

				if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length())
				{
					String bindingName = parameterName.substring(BINDING_PREFIX.length());
					Value bindingValue = ProtocolUtil.parseValueParam(request, parameterName,
							repository.getValueFactory());
					result.setBinding(bindingName, bindingValue);
				}
			}
		}
		catch (UnsupportedQueryLanguageException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Unsupported query language: " + queryLn);
		}
		catch (MalformedQueryException e) {
			throw new ClientRequestException(SC_BAD_REQUEST, "Malformed query: " + e.getMessage());
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR);
		}

		return result;
	}
}
