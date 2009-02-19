/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.MalformedQuery;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class QueriesClient {

	private HTTPConnectionPool queries;

	public QueriesClient(HTTPConnectionPool queries) {
		this.queries = queries;
	}

	public QueryClient postQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection method = queries.post();
		try {
			method.sendForm(getQueryParams(ql, query, baseURI));
			execute(method);
			String url = method.readLocation();
			String type = method.readQueryType();
			HTTPConnectionPool location = queries.location(url);
			if (Protocol.GRAPH_QUERY.equals(type))
				return new GraphQueryClient(location);
			if (Protocol.BOOLEAN_QUERY.equals(type))
				return new BooleanQueryClient(location);
			if (Protocol.BINDINGS_QUERY.equals(type))
				return new TupleQueryClient(location);
			throw new StoreException("Unsupported query type: " + type);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public GraphQueryClient postGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection method = queries.post();
		try {
			method.sendForm(getQueryParams(ql, query, baseURI));
			execute(method);
			String url = method.readLocation();
			assert Protocol.GRAPH_QUERY.equals(method.readQueryType());
			return new GraphQueryClient(queries.location(url));
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public BooleanQueryClient postBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection method = queries.post();
		try {
			method.sendForm(getQueryParams(ql, query, baseURI));
			execute(method);
			String url = method.readLocation();
			assert Protocol.BOOLEAN_QUERY.equals(method.readQueryType());
			return new BooleanQueryClient(queries.location(url));
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	public TupleQueryClient postTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection method = queries.post();
		try {
			method.sendForm(getQueryParams(ql, query, baseURI));
			execute(method);
			String url = method.readLocation();
			assert Protocol.BINDINGS_QUERY.equals(method.readQueryType());
			return new TupleQueryClient(queries.location(url));
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			method.release();
		}
	}

	private void execute(HTTPConnection method)
		throws IOException, StoreException, MalformedQueryException
	{
		try {
			method.execute();
		}
		catch (MalformedQuery e) {
			throw new MalformedQueryException(e);
		}
		catch (UnsupportedQueryLanguage e) {
			throw new UnsupportedQueryLanguageException(e);
		}
		catch (UnsupportedFileFormat e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (UnsupportedMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (Unauthorized e) {
			throw new UnauthorizedException(e);
		}
		catch (HTTPException e) {
			throw new StoreException(e);
		}
	}

	private List<NameValuePair> getQueryParams(QueryLanguage ql, String query, String baseURI) {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>();

		if (ql != null) {
			queryParams.add(new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.getName()));
		}
		if (baseURI != null) {
			queryParams.add(new NameValuePair(Protocol.BASEURI_PARAM_NAME, baseURI));
		}

		queryParams.add(new NameValuePair(Protocol.QUERY_PARAM_NAME, query));
		return queryParams;
	}

}
