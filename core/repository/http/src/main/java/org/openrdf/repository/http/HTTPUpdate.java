/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.repository.http;

import java.io.IOException;
import java.util.Iterator;

import org.openrdf.http.client.HTTPClient;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.query.impl.AbstractUpdate;
import org.openrdf.repository.RepositoryException;

/**
 * Update specific to the HTTP protocol. Methods in this class may throw the
 * specific RepositoryException subclass UnautorizedException, the semantics of
 * which is defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Jeen Broekstra
 */
public class HTTPUpdate extends AbstractUpdate {

	protected final HTTPRepositoryConnection httpCon;

	private final QueryLanguage queryLanguage;

	private final String queryString;

	private final String baseURI;

	public HTTPUpdate(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		this.httpCon = con;
		this.queryLanguage = ql;
		this.queryString = queryString;
		this.baseURI = baseURI;
	}

	protected Binding[] getBindingsArray() {
		BindingSet bindings = this.getBindings();

		Binding[] bindingsArray = new Binding[bindings.size()];

		Iterator<Binding> iter = bindings.iterator();
		for (int i = 0; i < bindings.size(); i++) {
			bindingsArray[i] = iter.next();
		}

		return bindingsArray;
	}

	@Override
	public String toString() {
		return getQueryString();
	}

	public void execute()
		throws UpdateExecutionException
	{
		try {
			if (httpCon.isAutoCommit()) {
				// execute update immediately
				HTTPClient client = httpCon.getRepository().getHTTPClient();
				try {
					client.sendUpdate(getQueryLanguage(), getQueryString(), getBaseURI(), dataset, includeInferred,
							getBindingsArray());
				}
				catch (UnauthorizedException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
				catch (QueryInterruptedException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
				catch (MalformedQueryException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
				catch (IOException e) {
					throw new HTTPUpdateExecutionException(e.getMessage(), e);
				}
			}
			else {
				// defer execution as part of transaction.
				httpCon.scheduleUpdate(this);
			}
		}
		catch (RepositoryException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}

	}

	/**
	 * @return Returns the baseURI.
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @return Returns the queryLanguage.
	 */
	public QueryLanguage getQueryLanguage() {
		return queryLanguage;
	}

	/**
	 * @return Returns the queryString.
	 */
	public String getQueryString() {
		return queryString;
	}
}
