/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008-2010.
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
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.UnsupportedQueryLanguageException;
import org.openrdf.rio.UnsupportedRDFormatException;
import org.openrdf.store.StoreException;

/**
 * @author James Leigh
 */
public class QueryClient {

	private final HTTPConnectionPool pool;

	private Dataset dataset;

	private boolean includeInferred;

	private BindingSet bindings;

	private int offset = 0;

	private int limit = -1;

	public QueryClient(HTTPConnectionPool pool) {
		this.pool = pool;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public boolean isIncludeInferred() {
		return includeInferred;
	}

	public void setIncludeInferred(boolean includeInferred) {
		this.includeInferred = includeInferred;
	}

	public BindingSet getBindingSet() {
		return bindings;
	}

	public void setBindingSet(BindingSet bindings) {
		this.bindings = bindings;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	protected HTTPConnection createConnection() {
		return pool.post();
	}

	protected HTTPConnection execute(HTTPConnection con)
		throws StoreException
	{
		try {
			con.sendForm(getQueryParams());
			try {
				con.execute();
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
			return con;
		}
		catch (IOException e) {
			throw new StoreException(e);
		}
	}

	protected <T> Future<T> submitTask(Callable<T> task) {
		return pool.submitTask(task);
	}

	private List<NameValuePair> getQueryParams() {
		List<NameValuePair> queryParams = new ArrayList<NameValuePair>(bindings.size() + 10);

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

		if (offset > 0) {
			queryParams.add(new NameValuePair(Protocol.OFFSET, String.valueOf(offset)));
		}
		if (limit >= 0) {
			queryParams.add(new NameValuePair(Protocol.LIMIT, String.valueOf(limit)));
		}

		for (Binding binding : bindings) {
			String paramName = Protocol.BINDING_PREFIX + binding.getName();
			String paramValue = Protocol.encodeValue(binding.getValue());
			queryParams.add(new NameValuePair(paramName, paramValue));
		}

		return queryParams;
	}
}
