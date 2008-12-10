/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.controllers;

import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.CONN_PATH;
import static org.openrdf.http.protocol.Protocol.DEFAULT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.NAMED_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.REPO_PATH;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.ClientHTTPException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.NotImplemented;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.http.server.BooleanQueryResult;
import org.openrdf.http.server.helpers.ProtocolUtil;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
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
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.query.impl.GraphQueryResultImpl;
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
	@RequestMapping(method = { GET, HEAD }, value = "/repositories")
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
		RepositoryManager repositoryManager = RepositoryInterceptor.getReadOnlyManager(request);
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

	@RequestMapping(method = POST, value = REPO_PATH + "/connections")
	public void post(HttpServletRequest request, HttpServletResponse response)
		throws StoreException
	{
		String id = RepositoryInterceptor.createConnection(request);
		StringBuffer url = request.getRequestURL();
		String location = url.append("/").append(id).toString();
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setHeader("Location", location);
	}

	@ModelAttribute
	@RequestMapping(method = DELETE, value = CONN_PATH)
	public void delete(HttpServletRequest request)
		throws StoreException
	{
		RepositoryInterceptor.closeConnection(request);
	}

	@ModelAttribute
	@RequestMapping(method = { GET, POST, HEAD }, value = { REPO_PATH, CONN_PATH })
	public QueryResult<?> query(HttpServletRequest request, HttpServletResponse response)
		throws HTTPException, IOException, StoreException, MalformedQueryException
	{
		Repository repository = RepositoryInterceptor.getRepository(request);
		RepositoryConnection repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);

		String contentType = request.getContentType();
		RequestMethod reqMethod = RequestMethod.valueOf(request.getMethod());
		if (contentType != null) {
			String mimeType = HttpServerUtil.getMIMEType(contentType);
			if (!Protocol.FORM_MIME_TYPE.equals(mimeType)) {
				throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
			}
			if (!POST.equals(reqMethod)) {
				// Include form data in parameters (already included for POST).
				request = ProtocolUtil.readFormData(request);
			}
		}

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
			int qryCode = String.valueOf(queryStr).hashCode();
			logger.info("Query {}", qryCode);
			logger.debug("Query {} = {}", qryCode, queryStr);
		}

		if (queryStr != null) {
			Query query = getQuery(repository, repositoryCon, queryStr, request, response);

			if (query instanceof TupleQuery) {
				TupleQuery tQuery = (TupleQuery)query;

				if (HEAD.equals(reqMethod)) {
					List<String> names = Collections.emptyList();
					Set<BindingSet> bindings = Collections.emptySet();
					return new TupleQueryResultImpl(names, bindings);
				}
				return tQuery.evaluate();
			}
			else if (query instanceof GraphQuery) {
				GraphQuery gQuery = (GraphQuery)query;

				if (HEAD.equals(reqMethod)) {
					Map<String, String> namespaces = Collections.emptyMap();
					Set<Statement> statements = Collections.emptySet();
					return new GraphQueryResultImpl(namespaces, statements);
				}
				return gQuery.evaluate();
			}
			else if (query instanceof BooleanQuery) {
				BooleanQuery bQuery = (BooleanQuery)query;

				// @ModelAttribute does not support a return type of boolean
				if (HEAD.equals(reqMethod))
					return BooleanQueryResult.EMPTY;
				return new BooleanQueryResult(bQuery.evaluate());
			}
			else {
				throw new NotImplemented("Unsupported query type: " + query.getClass().getName());
			}
		}
		else {
			throw new BadRequest("Missing parameter: " + QUERY_PARAM_NAME);
		}
	}

	private Query getQuery(Repository repository, RepositoryConnection repositoryCon, String queryStr,
			HttpServletRequest request, HttpServletResponse response)
		throws IOException, ClientHTTPException, StoreException, MalformedQueryException
	{
		Query result = null;

		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = request.getParameter(QUERY_LANGUAGE_PARAM_NAME);
		logger.debug("query language param = {}", queryLnStr);

		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr);

			if (queryLn == null) {
				throw new UnsupportedQueryLanguage("Unknown query language: " + queryLnStr);
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
						throw new BadRequest("Illegal URI for default graph: " + defaultGraphURI);
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
						throw new BadRequest("Illegal URI for named graph: " + namedGraphURI);
					}
				}
			}
		}

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

			if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length()) {
				String bindingName = parameterName.substring(BINDING_PREFIX.length());
				Value bindingValue = ProtocolUtil.parseValueParam(request, parameterName,
						repositoryCon.getValueFactory());
				result.setBinding(bindingName, bindingValue);
			}
		}

		return result;
	}
}
