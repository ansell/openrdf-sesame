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

	protected final QueryLanguage queryLanguage;

	protected final String queryString;

	protected final String baseURI;

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
		return queryString;
	}

	public void execute()
		throws UpdateExecutionException
	{
		HTTPClient client = httpCon.getRepository().getHTTPClient();
		try {
			client.sendUpdate(queryLanguage, queryString, baseURI, dataset, includeInferred, getBindingsArray());
		}
		catch (UnauthorizedException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}
		catch (QueryInterruptedException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}
		catch (RepositoryException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}
		catch (MalformedQueryException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}
		catch (IOException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}
	}
}
