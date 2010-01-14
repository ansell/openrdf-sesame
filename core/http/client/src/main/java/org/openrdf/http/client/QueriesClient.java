/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
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

	private final HTTPConnectionPool pool;

	public QueriesClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public QueryClient postQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection con = pool.post();
		try {
			con.sendForm(getQueryParams(ql, query, baseURI));
			execute(con);
			String url = con.readLocation();
			String type = con.readQueryType();
			HTTPConnectionPool location = pool.location(url);
			if (Protocol.GRAPH_QUERY.equals(type)) {
				return new GraphQueryClient(location);
			}
			if (Protocol.BOOLEAN_QUERY.equals(type)) {
				return new BooleanQueryClient(location);
			}
			if (Protocol.BINDINGS_QUERY.equals(type)) {
				return new TupleQueryClient(location);
			}
			throw new StoreException("Unsupported query type: " + type);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public GraphQueryClient postGraphQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection con = pool.post();
		try {
			con.sendForm(getQueryParams(ql, query, baseURI));
			execute(con);
			String url = con.readLocation();
			assert Protocol.GRAPH_QUERY.equals(con.readQueryType());
			return new GraphQueryClient(pool.location(url));
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public BooleanQueryClient postBooleanQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection con = pool.post();
		try {
			con.sendForm(getQueryParams(ql, query, baseURI));
			execute(con);
			String url = con.readLocation();
			assert Protocol.BOOLEAN_QUERY.equals(con.readQueryType());
			return new BooleanQueryClient(pool.location(url));
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	public TupleQueryClient postTupleQuery(QueryLanguage ql, String query, String baseURI)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection con = pool.post();
		try {
			con.sendForm(getQueryParams(ql, query, baseURI));
			execute(con);
			String url = con.readLocation();
			assert Protocol.BINDINGS_QUERY.equals(con.readQueryType());
			return new TupleQueryClient(pool.location(url));
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			con.release();
		}
	}

	private void execute(HTTPConnection con)
		throws IOException, StoreException, MalformedQueryException
	{
		try {
			con.execute();
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
