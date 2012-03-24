/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.repository;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.DEFAULT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.NAMED_GRAPH_PARAM_NAME;
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

import info.aduna.lang.FileFormat;
import info.aduna.lang.service.FileFormatServiceRegistry;
import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.server.ClientHTTPException;
import org.openrdf.http.server.ProtocolUtil;
import org.openrdf.http.server.ServerHTTPException;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.resultio.BooleanQueryResultWriterRegistry;
import org.openrdf.query.resultio.TupleQueryResultWriterRegistry;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.rio.RDFWriterRegistry;

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

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		int qryCode = 0;
		if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
			qryCode = String.valueOf(queryStr).hashCode();
		}

		String reqMethod = request.getMethod();
		if (METHOD_GET.equals(reqMethod)) {
			logger.info("GET query {}", qryCode);
		}
		else if (METHOD_POST.equals(reqMethod)) {
			logger.info("POST query {}", qryCode);

			String mimeType = HttpServerUtil.getMIMEType(request.getContentType());
			if (!Protocol.FORM_MIME_TYPE.equals(mimeType)) {
				throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: " + mimeType);
			}
		}
		logger.debug("query {} = {}", qryCode, queryStr);

		if (queryStr != null) {
			Query query = getQuery(repository, repositoryCon, queryStr, request, response);

			View view;
			Object queryResult;
			FileFormatServiceRegistry<? extends FileFormat, ?> registry;

			try {
				if (query instanceof TupleQuery) {
					TupleQuery tQuery = (TupleQuery)query;
	
					queryResult = tQuery.evaluate();
					registry = TupleQueryResultWriterRegistry.getInstance();
					view = TupleQueryResultView.getInstance();
				}
				else if (query instanceof GraphQuery) {
					GraphQuery gQuery = (GraphQuery)query;
	
					queryResult = gQuery.evaluate();
					registry = RDFWriterRegistry.getInstance();
					view = GraphQueryResultView.getInstance();
				}
				else if (query instanceof BooleanQuery) {
					BooleanQuery bQuery = (BooleanQuery)query;
	
					queryResult = bQuery.evaluate();
					registry = BooleanQueryResultWriterRegistry.getInstance();
					view = BooleanQueryResultView.getInstance();
				}
				else {
					throw new ClientHTTPException(SC_BAD_REQUEST, "Unsupported query type: "
							+ query.getClass().getName());
				}
			}
			catch (QueryInterruptedException e) {
				logger.info("Query interrupted", e);
				throw new ServerHTTPException(SC_SERVICE_UNAVAILABLE, "Query evaluation took too long");
			}
			catch (QueryEvaluationException e) {
				logger.info("Query evaluation error", e);
				throw new ServerHTTPException("Query evaluation error: " + e.getMessage());
			}

			Object factory = ProtocolUtil.getAcceptableService(request, response, registry);

			Map<String, Object> model = new HashMap<String, Object>();
			model.put(QueryResultView.FILENAME_HINT_KEY, "query-result");
			model.put(QueryResultView.QUERY_RESULT_KEY, queryResult);
			model.put(QueryResultView.FACTORY_KEY, factory);

			return new ModelAndView(view, model);
		}
		else {
			throw new ClientHTTPException(SC_BAD_REQUEST, "Missing parameter: " + QUERY_PARAM_NAME);
		}
	}

	private Query getQuery(Repository repository, RepositoryConnection repositoryCon, String queryStr,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientHTTPException
	{
		Query result = null;

		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		logger.debug("query language param = {}", queryLnStr);

		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr);

			if (queryLn == null) {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Unknown query language: " + queryLnStr);
			}
		}

		String baseURI = request.getParameter(Protocol.BASEURI_PARAM_NAME);

		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		String timeout = request.getParameter(Protocol.TIMEOUT_PARAM_NAME);
		int maxQueryTime = 0;
		if (timeout != null) {
			try {
				maxQueryTime = Integer.parseInt(timeout);
			}
			catch (NumberFormatException e) {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Invalid timeout value: " + timeout);
			}
		}

		// build a dataset, if specified
		String[] defaultGraphURIs = request.getParameterValues(DEFAULT_GRAPH_PARAM_NAME);
		String[] namedGraphURIs = request.getParameterValues(NAMED_GRAPH_PARAM_NAME);

		DatasetImpl dataset = null;
		if (defaultGraphURIs != null || namedGraphURIs != null) {
			dataset = new DatasetImpl();

			if (defaultGraphURIs != null) {
				for (String defaultGraphURI : defaultGraphURIs) {
					try {
						URI uri = createURIOrNull(repository, defaultGraphURI);
						dataset.addDefaultGraph(uri);
					}
					catch (IllegalArgumentException e) {
						throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for default graph: "
								+ defaultGraphURI);
					}
				}
			}

			if (namedGraphURIs != null) {
				for (String namedGraphURI : namedGraphURIs) {
					try {
						URI uri = createURIOrNull(repository, namedGraphURI);
						dataset.addNamedGraph(uri);
					}
					catch (IllegalArgumentException e) {
						throw new ClientHTTPException(SC_BAD_REQUEST, "Illegal URI for named graph: "
								+ namedGraphURI);
					}
				}
			}
		}

		try {
			result = repositoryCon.prepareQuery(queryLn, queryStr, baseURI);

			result.setIncludeInferred(includeInferred);

			if (maxQueryTime > 0) {
				result.setMaxQueryTime(maxQueryTime);
			}

			if (dataset != null) {
				result.setDataset(dataset);
			}

			// determine if any variable bindings have been set on this query.
			@SuppressWarnings("unchecked")
			Enumeration<String> parameterNames = request.getParameterNames();

			while (parameterNames.hasMoreElements()) {
				String parameterName = parameterNames.nextElement();

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
			ErrorInfo errInfo = new ErrorInfo(ErrorType.UNSUPPORTED_QUERY_LANGUAGE, queryLn.getName());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (MalformedQueryException e) {
			ErrorInfo errInfo = new ErrorInfo(ErrorType.MALFORMED_QUERY, e.getMessage());
			throw new ClientHTTPException(SC_BAD_REQUEST, errInfo.toString());
		}
		catch (RepositoryException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR);
		}

		return result;
	}

	/**
	 * @param repository
	 * @param graphURI
	 * @return
	 */
	private URI createURIOrNull(Repository repository, String graphURI) {
		if ("null".equals(graphURI))
			return null;
		return repository.getValueFactory().createURI(graphURI);
	}
}
