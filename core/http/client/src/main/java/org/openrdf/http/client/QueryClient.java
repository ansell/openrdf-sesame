/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.http.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import org.apache.commons.httpclient.NameValuePair;

import org.openrdf.http.client.connections.HTTPConnection;
import org.openrdf.http.client.connections.HTTPConnectionPool;
import org.openrdf.http.protocol.Protocol;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.exceptions.HTTPException;
import org.openrdf.http.protocol.exceptions.Unauthorized;
import org.openrdf.http.protocol.exceptions.UnsupportedFileFormat;
import org.openrdf.http.protocol.exceptions.UnsupportedMediaType;
import org.openrdf.http.protocol.exceptions.UnsupportedQueryLanguage;
import org.openrdf.model.URI;
import org.openrdf.query.Binding;
import org.openrdf.query.Dataset;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class QueryClient {

	private HTTPConnectionPool query;

	public QueryClient(HTTPConnectionPool query) {
		this.query = query;
	}

	protected HTTPConnection get() {
		return query.post();
	}

	protected HTTPConnection execute(HTTPConnection method, Dataset dataset, boolean includeInferred,
			Binding... bindings)
		throws StoreException
	{
		try {
			method.sendForm(getQueryParams(dataset, includeInferred, bindings));
			execute(method);
			return method;
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}

	protected <T> Future<T> submitTask(Callable<T> task) {
		return query.submitTask(task);
	}

	private List<NameValuePair> getQueryParams(Dataset dataset, boolean includeInferred, Binding... bindings) {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>(bindings.length + 10);

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

	void execute(HTTPConnection method)
		throws IOException, StoreException
	{
		try {
			method.execute();
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

}
