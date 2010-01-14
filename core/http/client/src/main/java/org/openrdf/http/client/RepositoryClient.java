/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2002-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPRequest;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.FutureGraphQueryResult;
import org.openrdf.http.client.helpers.FutureTupleQueryResult;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.MalformedQuery;
import org.openrdf.http.protocol.exceptions.NoCompatibleMediaType;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.query.resultio.QueryResultParseException;
import org.openrdf.result.GraphResult;
import org.openrdf.result.TupleResult;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * Low-level HTTP client for Sesame's HTTP protocol. Methods correspond directly
 * to the functionality offered by the protocol.
 * 
 * @author Herko ter Horst
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class RepositoryClient {

	private final HTTPConnectionPool pool;

	public RepositoryClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	@Override
	public String toString() {
		return pool.getURL();
	}

	public ConnectionsClient connections() {
		return new ConnectionsClient(pool.slash(Protocol.CONNECTIONS));
	}

	public MetaDataClient metadata() {
		return new MetaDataClient(pool.slash(Protocol.METADATA));
	}

	public ContextClient contexts() {
		return new ContextClient(pool.slash(Protocol.CONTEXTS));
	}

	public NamespaceClient namespaces() {
		return new NamespaceClient(pool.slash(Protocol.NAMESPACES));
	}

	public SizeClient size() {
		return new SizeClient(pool.slash(Protocol.SIZE));
	}

	public StatementClient statements() {
		return new StatementClient(pool.slash(Protocol.STATEMENTS));
	}

	/*------------------*
	 * Query evaluation *
	 *------------------*/

	public String getQueryType(QueryLanguage ql, String query)
		throws StoreException, MalformedQueryException
	{
		HTTPRequest request = pool.head();

		try {
			request.acceptBoolean();
			request.acceptTupleQueryResult();
			request.acceptGraphQueryResult();
			request.sendForm(getQueryParams(ql, query, null, true));
			execute(request);
			return request.readQueryType();
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		finally {
			request.release();
		}
	}

	public TupleResult sendTupleQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws StoreException, MalformedQueryException
	{
		final HTTPRequest request = pool.post();
		request.sendForm(getQueryParams(ql, query, dataset, includeInferred, bindings));
		Callable<TupleResult> task = new Callable<TupleResult>() {

			public TupleResult call()
				throws Exception
			{
				try {
					request.acceptTupleQueryResult();
					execute(request);
					return request.getTupleQueryResult();
				}
				catch (NoCompatibleMediaType e) {
					throw new UnsupportedRDFormatException(e);
				}
			}
		};
		return new FutureTupleQueryResult(pool.submitTask(task));
	}

	public void sendTupleQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			TupleQueryResultHandler handler, Binding... bindings)
		throws TupleQueryResultHandlerException, StoreException, MalformedQueryException
	{
		HTTPRequest request = pool.post();

		try {
			request.acceptTupleQueryResult();
			request.sendForm(getQueryParams(ql, query, dataset, includeInferred, bindings));
			execute(request);
			request.readTupleQueryResult(handler);
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
		finally {
			request.release();
		}
	}

	public GraphResult sendGraphQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws StoreException, MalformedQueryException
	{
		final HTTPRequest request = pool.post();
		request.sendForm(getQueryParams(ql, query, dataset, includeInferred, bindings));
		Callable<GraphResult> task = new Callable<GraphResult>() {

			public GraphResult call()
				throws Exception
			{
				try {
					request.acceptGraphQueryResult();
					execute(request);
					return request.getGraphQueryResult();
				}
				catch (NoCompatibleMediaType e) {
					throw new UnsupportedRDFormatException(e);
				}
			}
		};
		return new FutureGraphQueryResult(pool.submitTask(task));
	}

	public void sendGraphQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			RDFHandler handler, Binding... bindings)
		throws RDFHandlerException, StoreException, MalformedQueryException
	{
		HTTPRequest request = pool.post();

		try {
			request.acceptRDF(false);
			request.sendForm(getQueryParams(ql, query, dataset, includeInferred, bindings));
			execute(request);
			request.readRDF(handler);
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (RDFParseException e) {
			throw new StoreException(e);
		}
		finally {
			request.release();
		}
	}

	public boolean sendBooleanQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			Binding... bindings)
		throws StoreException, MalformedQueryException
	{
		HTTPRequest request = pool.post();

		try {
			request.acceptBoolean();
			request.sendForm(getQueryParams(ql, query, dataset, includeInferred, bindings));
			execute(request);
			return request.readBoolean();
		}
		catch (NoCompatibleMediaType e) {
			throw new UnsupportedRDFormatException(e);
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
		catch (QueryResultParseException e) {
			throw new StoreException(e);
		}
		finally {
			request.release();
		}
	}

	void execute(HTTPRequest request)
		throws IOException, StoreException, MalformedQueryException
	{
		try {
			request.execute();
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

	private List<NameValuePair> getQueryParams(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>(bindings.length + 10);

		queryParams.add(new NameValuePair(Protocol.QUERY_LANGUAGE_PARAM_NAME, ql.getName()));
		queryParams.add(new NameValuePair(Protocol.QUERY_PARAM_NAME, query));
		queryParams.add(new NameValuePair(Protocol.INCLUDE_INFERRED_PARAM_NAME,
				Boolean.toString(includeInferred)));

		if (dataset != null) {
			for (URI defaultGraphURI : dataset.getDefaultGraphs()) {
				queryParams.add(new NameValuePair(Protocol.DEFAULT_GRAPH_PARAM_NAME, defaultGraphURI.toString()));
			}
			for (URI namedGraphURI : dataset.getNamedGraphs()) {
				queryParams.add(new NameValuePair(Protocol.NAMED_GRAPH_PARAM_NAME, namedGraphURI.toString()));
			}
		}

		for (int i = 0; i < bindings.length; i++) {
			String paramName = Protocol.BINDING_PREFIX + bindings[i].getName();
			String paramValue = Protocol.encodeValue(bindings[i].getValue());
			queryParams.add(new NameValuePair(paramName, paramValue));
		}

		return queryParams;
	}
}
