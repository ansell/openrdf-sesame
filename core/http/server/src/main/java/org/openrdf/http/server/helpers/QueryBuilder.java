/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.helpers;

import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.DEFAULT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.NAMED_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import info.aduna.webapp.util.HttpServerUtil;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.exceptions.BadRequest;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.http.server.repository.RepositoryInterceptor;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class QueryBuilder {

	private Logger logger = LoggerFactory.getLogger(QueryBuilder.class);

	private HttpServletRequest request;

	private RepositoryConnection repositoryCon;

	private ValueFactory vf;

	public QueryBuilder(HttpServletRequest request)
		throws IOException, UnsupportedMediaType, StoreException
	{
		this.request = request;
		this.repositoryCon = RepositoryInterceptor.getReadOnlyConnection(request);
		this.vf = repositoryCon.getValueFactory();
		String contentType = request.getContentType();
		if (contentType != null) {
			String mimeType = HttpServerUtil.getMIMEType(contentType);
			if (!Protocol.FORM_MIME_TYPE.equals(mimeType)) {
				throw new UnsupportedMediaType("Unsupported MIME type: " + mimeType);
			}
			RequestMethod reqMethod = RequestMethod.valueOf(request.getMethod());
			if (!POST.equals(reqMethod)) {
				// Include form data in parameters (already included for POST).
				request = ProtocolUtil.readFormData(request);
			}
		}
	}

	public Query prepareQuery()
		throws BadRequest, StoreException, MalformedQueryException
	{

		String queryStr = request.getParameter(QUERY_PARAM_NAME);
		if (queryStr == null)
			throw new BadRequest("Missing parameter: " + QUERY_PARAM_NAME);

		if (logger.isInfoEnabled() || logger.isDebugEnabled()) {
			int qryCode = String.valueOf(queryStr).hashCode();
			logger.info("Query {}", qryCode);
			logger.debug("Query {} = {}", qryCode, queryStr);
		}

		QueryLanguage queryLn = getQueryLanguage();
		String baseURI = request.getParameter(BASEURI_PARAM_NAME);

		Query result = repositoryCon.prepareQuery(queryLn, queryStr, baseURI);

		return prepareQuery(result);
	}

	public Query prepareQuery(Query result)
		throws BadRequest
	{
		// determine if inferred triples should be included in query evaluation
		boolean includeInferred = ProtocolUtil.parseBooleanParam(request, INCLUDE_INFERRED_PARAM_NAME, true);
		result.setIncludeInferred(includeInferred);

		DatasetImpl dataset = getDataset();
		if (dataset != null) {
			result.setDataset(dataset);
		}
		setBindings(result);
		return result;
	}

	private QueryLanguage getQueryLanguage()
		throws UnsupportedQueryLanguage
	{
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
		return queryLn;
	}

	private DatasetImpl getDataset()
		throws BadRequest
	{
		// build a dataset, if specified
		String[] defaultGraphURIs = request.getParameterValues(DEFAULT_GRAPH_PARAM_NAME);
		String[] namedGraphURIs = request.getParameterValues(NAMED_GRAPH_PARAM_NAME);

		DatasetImpl dataset = null;
		if (defaultGraphURIs != null || namedGraphURIs != null) {
			dataset = new DatasetImpl();

			if (defaultGraphURIs != null) {
				for (String defaultGraphURI : defaultGraphURIs) {
					try {
						URI uri = vf.createURI(defaultGraphURI);
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
						URI uri = vf.createURI(namedGraphURI);
						dataset.addNamedGraph(uri);
					}
					catch (IllegalArgumentException e) {
						throw new BadRequest("Illegal URI for named graph: " + namedGraphURI);
					}
				}
			}
		}
		return dataset;
	}

	private void setBindings(Query result)
		throws BadRequest
	{
		// determine if any variable bindings have been set on this query.
		@SuppressWarnings("unchecked")
		Enumeration<String> parameterNames = request.getParameterNames();

		while (parameterNames.hasMoreElements()) {
			String parameterName = parameterNames.nextElement();

			if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length()) {
				String bindingName = parameterName.substring(BINDING_PREFIX.length());
				Value bindingValue = ProtocolUtil.parseValueParam(request, parameterName, vf);
				result.setBinding(bindingName, bindingValue);
			}
		}
	}

}
