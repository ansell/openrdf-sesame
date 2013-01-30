/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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
