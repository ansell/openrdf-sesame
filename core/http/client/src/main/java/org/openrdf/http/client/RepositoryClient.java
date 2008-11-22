/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.client.helpers.ContextClient;
import org.openrdf.http.client.helpers.NamespaceClient;
import org.openrdf.http.client.helpers.SizeClient;
import org.openrdf.http.client.helpers.StatementClient;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.GraphQueryResult;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.TupleQueryResultHandler;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.impl.GraphQueryResultImpl;
import org.openrdf.query.impl.TupleQueryResultBuilder;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.helpers.StatementCollector;
import org.openrdf.store.StoreException;

/**
 * Low-level HTTP client for Sesame's HTTP protocol. Methods correspond directly
 * to the functionality offered by the protocol.
 * 
 * @author James Leigh
 */
public class RepositoryClient {

	private HTTPConnectionPool repository;

	public RepositoryClient(String repositoryURL) {
		this.repository = new HTTPConnectionPool(repositoryURL);
	}

	public RepositoryClient(HTTPConnectionPool repository) {
		this.repository = repository;
	}

	public String getURL() {
		return repository.getURL();
	}

	public void setUsernameAndPassword(String username, String password) {
		repository.setUsernameAndPassword(username, password);
	}

	public ValueFactory getValueFactory() {
		return repository.getValueFactory();
	}

	public void setValueFactory(ValueFactory valueFactory) {
		repository.setValueFactory(valueFactory);
	}

	public HTTPConnectionPool getPool() {
		return repository;
	}

	public ContextClient contexts() {
		return new ContextClient(repository.slash("contexts"));
	}

	public NamespaceClient namespaces() {
		return new NamespaceClient(repository.slash("namespaces"));
	}

	public SizeClient size() {
		return new SizeClient(repository.slash("size"));
	}

	public StatementClient statements() {
		return new StatementClient(repository.slash("statements"));
	}

	/*------------------*
	 * Query evaluation *
	 *------------------*/

	public TupleQueryResult sendTupleQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws StoreException, MalformedQueryException
	{
		try {
			TupleQueryResultBuilder builder = new TupleQueryResultBuilder();
			sendTupleQuery(ql, query, dataset, includeInferred, builder, bindings);
			return builder.getQueryResult();
		}
		catch (TupleQueryResultHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendTupleQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			TupleQueryResultHandler handler, Binding... bindings)
		throws TupleQueryResultHandlerException, StoreException, MalformedQueryException,
		UnauthorizedException
	{
		HTTPConnection method = getQueryMethod(ql, query, dataset, includeInferred, bindings);

		try {
			method.acceptTuple();
			method.execute();
			method.readTuple(handler);
		}
		finally {
			method.release();
		}
	}

	public GraphQueryResult sendGraphQuery(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
		throws StoreException, MalformedQueryException
	{
		try {
			StatementCollector collector = new StatementCollector();
			sendGraphQuery(ql, query, dataset, includeInferred, collector, bindings);
			return new GraphQueryResultImpl(collector.getNamespaces(), collector.getStatements());
		}
		catch (RDFHandlerException e) {
			// Found a bug in TupleQueryResultBuilder?
			throw new RuntimeException(e);
		}
	}

	public void sendGraphQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			RDFHandler handler, Binding... bindings)
		throws RDFHandlerException, StoreException, MalformedQueryException
	{
		HTTPConnection method = getQueryMethod(ql, query, dataset, includeInferred, bindings);

		try {
			method.acceptRDF(false);
			method.executeQuery();
			method.readRDF(handler);
		}
		finally {
			method.release();
		}
	}

	public boolean sendBooleanQuery(QueryLanguage ql, String query, Dataset dataset, boolean includeInferred,
			Binding... bindings)
		throws StoreException, MalformedQueryException
	{
		HTTPConnection method = getQueryMethod(ql, query, dataset, includeInferred, bindings);

		try {
			method.acceptBoolean();
			method.executeQuery();
			return method.readBoolean();
		}
		finally {
			method.release();
		}
	}

	protected HTTPConnection getQueryMethod(QueryLanguage ql, String query, Dataset dataset,
			boolean includeInferred, Binding... bindings)
	{
		HTTPConnection method = repository.post();

		method.sendForm(getQueryMethodParameters(ql, query, dataset, includeInferred,
				bindings));

		return method;
	}

	protected List<NameValuePair> getQueryMethodParameters(QueryLanguage ql, String query, Dataset dataset,
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
