/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.DEFAULT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.NAMED_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.error.ErrorInfo;
import org.openrdf.http.protocol.error.ErrorType;
import org.openrdf.http.server.BooleanQueryResult;
import org.openrdf.http.server.exceptions.ClientHTTPException;
import org.openrdf.http.server.exceptions.HTTPException;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.ListBindingSet;
import org.openrdf.query.impl.TupleQueryResultImpl;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.manager.RepositoryInfo;
import org.openrdf.repository.manager.RepositoryManager;
import org.openrdf.store.StoreConfigException;
import org.openrdf.store.StoreException;

/**
 * Handles queries on a repository and renders the results in a format suitable
 * to the type of query.
 * 
 * @author Herko ter Horst
 * @author James Leigh
 */
@Controller
public class RepositoryController {

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@ModelAttribute
	@RequestMapping(value = "/repositories", method = RequestMethod.GET)
	public TupleQueryResult list(HttpServletRequest request)
		throws HTTPException, StoreConfigException
	{
		List<String> bindingNames = Arrays.asList("uri", "id", "title", "readable", "writable");
		List<BindingSet> bindingSets = new ArrayList<BindingSet>();

		// Determine the repository's URI
		StringBuffer requestURL = request.getRequestURL();
		if (requestURL.charAt(requestURL.length() - 1) != '/') {
			requestURL.append('/');
		}
		String namespace = requestURL.toString();

		ValueFactory vf = new ValueFactoryImpl();
		RepositoryManager repositoryManager = RepositoryInterceptor.getRepositoryManager(request);
		for (RepositoryInfo info : repositoryManager.getAllRepositoryInfos()) {
			String id = info.getId();
			URI uri = vf.createURI(namespace, id);
			Literal idLit = vf.createLiteral(id);
			Literal title = vf.createLiteral(info.getDescription());
			Literal readable = vf.createLiteral(info.isReadable());
			Literal writable = vf.createLiteral(info.isWritable());

			BindingSet bindings = new ListBindingSet(bindingNames, uri, idLit, title, readable, writable);
			bindingSets.add(bindings);
		}

		return new TupleQueryResultImpl(bindingNames, bindingSets);
	}

	@ModelAttribute
	@RequestMapping(value = "/repositories/*", method = { RequestMethod.GET, RequestMethod.POST })
	public QueryResult<?> query(HttpServletRequest request, HttpServletResponse response)
		throws ClientHTTPException, IOException, StoreException
	{
		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getRepositoryConnection(request);

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		int qryCode = 0;
		if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
			qryCode = String.valueOf(queryStr).hashCode();
		}

		String reqMethod = request.getMethod();
		if (RequestMethod.GET.equals(RequestMethod.valueOf(reqMethod))) {
			logger.info("GET query {}", qryCode);
		}
		else if (RequestMethod.POST.equals(RequestMethod.valueOf(reqMethod))) {
			logger.info("POST query {}", qryCode);

			String mimeType = HttpServerUtil.getMIMEType(request.getContentType());
			if (!Protocol.FORM_MIME_TYPE.equals(mimeType)) {
				throw new ClientHTTPException(SC_UNSUPPORTED_MEDIA_TYPE, "Unsupported MIME type: " + mimeType);
			}
		}
		logger.debug("query {} = {}", qryCode, queryStr);

		if (queryStr != null) {
			Query query = getQuery(repository, repositoryCon, queryStr, request, response);

			if (query instanceof TupleQuery) {
				TupleQuery tQuery = (TupleQuery)query;

				return tQuery.evaluate();
			}
			else if (query instanceof GraphQuery) {
				GraphQuery gQuery = (GraphQuery)query;

				return gQuery.evaluate();
			}
			else if (query instanceof BooleanQuery) {
				BooleanQuery bQuery = (BooleanQuery)query;

				// @ModelAttribute does not support a return type of boolean
				return new BooleanQueryResult(bQuery.evaluate());
			}
			else {
				throw new ClientHTTPException(SC_BAD_REQUEST, "Unsupported query type: "
						+ query.getClass().getName());
			}
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

		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);

		// build a dataset, if specified
		String[] defaultGraphURIs = request.getParameterValues(DEFAULT_GRAPH_PARAM_NAME);
		String[] namedGraphURIs = request.getParameterValues(NAMED_GRAPH_PARAM_NAME);

		DatasetImpl dataset = null;
		if (defaultGraphURIs != null || namedGraphURIs != null) {
			dataset = new DatasetImpl();

			if (defaultGraphURIs != null) {
				for (String defaultGraphURI : defaultGraphURIs) {
					try {
						URI uri = repositoryCon.getValueFactory().createURI(defaultGraphURI);
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
						URI uri = repositoryCon.getValueFactory().createURI(namedGraphURI);
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
			result = repositoryCon.prepareQuery(queryLn, queryStr);
			result.setIncludeInferred(includeInferred);

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
							repositoryCon.getValueFactory());
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
		catch (StoreException e) {
			logger.error("Repository error", e);
			response.sendError(SC_INTERNAL_SERVER_ERROR);
		}

		return result;
	}
}
