/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.server.filters;

import static org.openrdf.http.protocol.Protocol.BASEURI_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.BINDINGS_QUERY;
import static org.openrdf.http.protocol.Protocol.BINDING_PREFIX;
import static org.openrdf.http.protocol.Protocol.BOOLEAN_QUERY;
import static org.openrdf.http.protocol.Protocol.DEFAULT_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.GRAPH_QUERY;
import static org.openrdf.http.protocol.Protocol.INCLUDE_INFERRED_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.NAMED_GRAPH_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_LANGUAGE_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.QUERY_PARAM_NAME;
import static org.openrdf.http.protocol.Protocol.X_QUERY_TYPE;
import static org.openrdf.http.protocol.error.ErrorType.MALFORMED_QUERY;
import static org.openrdf.http.protocol.error.ErrorType.UNSUPPORTED_QUERY_LANGUAGE;
import static org.restlet.data.Status.CLIENT_ERROR_BAD_REQUEST;
import static org.restlet.data.Status.SERVER_ERROR_INTERNAL;

import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.server.ErrorInfoException;
import org.openrdf.http.server.helpers.RequestAtt;
import org.openrdf.http.server.helpers.ServerUtil;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.GraphQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.Query;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.impl.DatasetImpl;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.store.StoreException;

/**
 * Filter that parses textual queries to {@link Query} objects and adds this
 * object the a request's attributes. This filter will produce an appropriate
 * HTTP error when the query cannot be parsed.
 * 
 * @author Arjohn Kampman
 */
public class QueryParser extends Filter {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public QueryParser(Context context, Class<? extends Handler> next) {
		this(context, new Finder(context, next));
	}

	public QueryParser(Context context, Restlet next) {
		super(context, next);
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		try {
			Form params = ServerUtil.getParameters(request);

			if (params != null) {
				Query query = parseQuery(params, request, response);
				RequestAtt.setQuery(request, query);
			}

			return Filter.CONTINUE;
		}
		catch (ResourceException e) {
			response.setStatus(e.getStatus(), e.getMessage());
			return Filter.STOP;
		}
	}

	@Override
	protected void afterHandle(Request request, Response response) {
		if (response.getStatus().isSuccess()) {
			Query query = RequestAtt.getQuery(request);

			if (query != null) {
				setQueryTypeHeader(query, response);
			}
		}
	}

	private Query parseQuery(Form params, Request request, Response response)
		throws ResourceException
	{
		String queryStr = params.getFirstValue(Protocol.QUERY_PARAM_NAME);

		if (queryStr == null) {
			throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Missing parameter: " + QUERY_PARAM_NAME);
		}

		QueryLanguage queryLn = getQueryLanguage(params, response);
		String baseURI = params.getFirstValue(BASEURI_PARAM_NAME);

		try {
			RepositoryConnection connection = RequestAtt.getConnection(request);
			Query query = connection.prepareQuery(queryLn, queryStr, baseURI);
			parseQueryParameters(query, params, connection.getValueFactory());
			return query;
		}
		catch (StoreException e) {
			throw new ResourceException(SERVER_ERROR_INTERNAL, "Failed to prepare query", e);
		}
		catch (MalformedQueryException e) {
			throw new ErrorInfoException(MALFORMED_QUERY, e.getMessage());
		}
	}

	private QueryLanguage getQueryLanguage(Form params, Response response)
		throws ResourceException
	{
		// default query language is SPARQL
		QueryLanguage queryLn = QueryLanguage.SPARQL;

		String queryLnStr = params.getFirstValue(QUERY_LANGUAGE_PARAM_NAME);

		if (queryLnStr != null) {
			queryLn = QueryLanguage.valueOf(queryLnStr);

			if (queryLn == null) {
				throw new ErrorInfoException(UNSUPPORTED_QUERY_LANGUAGE, "Unknown query language: " + queryLnStr);
			}
		}

		return queryLn;
	}

	public static void parseQueryParameters(Query query, Form params, ValueFactory vf)
		throws ResourceException
	{
		boolean includeInferred = ServerUtil.parseBooleanParam(params, INCLUDE_INFERRED_PARAM_NAME,
				query.getIncludeInferred());
		query.setIncludeInferred(includeInferred);

		DatasetImpl dataset = parseDataset(params, vf);
		if (dataset != null) {
			query.setDataset(dataset);
		}

		if (query instanceof TupleQuery) {
			TupleQuery tupleQuery = (TupleQuery)query;

			int offset = ServerUtil.parseIntegerParam(params, Protocol.OFFSET, tupleQuery.getOffset());
			int limit = ServerUtil.parseIntegerParam(params, Protocol.LIMIT, tupleQuery.getLimit());
			tupleQuery.setOffset(offset);
			tupleQuery.setLimit(limit);
		}

		setBindings(query, params, vf);
	}

	private static DatasetImpl parseDataset(Form params, ValueFactory vf)
		throws ResourceException
	{
		// build a dataset, if specified
		String[] defaultGraphURIs = params.getValuesArray(DEFAULT_GRAPH_PARAM_NAME);
		String[] namedGraphURIs = params.getValuesArray(NAMED_GRAPH_PARAM_NAME);

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
						throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Illegal URI for default graph: "
								+ defaultGraphURI);
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
						throw new ResourceException(CLIENT_ERROR_BAD_REQUEST, "Illegal URI for named graph: "
								+ namedGraphURI);
					}
				}
			}
		}

		return dataset;
	}

	private static void setBindings(Query result, Form params, ValueFactory vf)
		throws ResourceException
	{
		// determine if any variable bindings have been set on this query.
		for (String parameterName : params.getNames()) {
			if (parameterName.startsWith(BINDING_PREFIX) && parameterName.length() > BINDING_PREFIX.length()) {
				String bindingName = parameterName.substring(BINDING_PREFIX.length());
				Value bindingValue = ServerUtil.parseValueParam(params, parameterName, vf);
				result.setBinding(bindingName, bindingValue);
			}
		}
	}

	private void setQueryTypeHeader(Query query, Response resonse) {
		assert query != null;

		String queryTypeValue = null;
		if (query instanceof TupleQuery) {
			queryTypeValue = BINDINGS_QUERY;
		}
		else if (query instanceof GraphQuery) {
			queryTypeValue = GRAPH_QUERY;
		}
		else if (query instanceof BooleanQuery) {
			queryTypeValue = BOOLEAN_QUERY;
		}
		else {
			logger.warn("Encountered unknown query type: {}", query.getClass().getName());
		}

		if (queryTypeValue != null) {
			Form responseHeaders = (Form)resonse.getAttributes().get("org.restlet.http.headers");

			if (responseHeaders == null) {
				responseHeaders = new Form();
				resonse.getAttributes().put("org.restlet.http.headers", responseHeaders);
			}

			responseHeaders.add(X_QUERY_TYPE, queryTypeValue);
		}
	}
}
