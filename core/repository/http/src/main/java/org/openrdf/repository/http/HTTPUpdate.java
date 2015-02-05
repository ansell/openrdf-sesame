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

import org.openrdf.http.client.SparqlSession;
import org.openrdf.http.client.query.AbstractHTTPUpdate;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.http.protocol.Protocol.Action;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;

/**
 * Update specific to the HTTP protocol. Methods in this class may throw the
 * specific RepositoryException subclass UnautorizedException, the semantics of
 * which is defined by the HTTP protocol.
 * 
 * @see org.openrdf.http.protocol.UnauthorizedException
 * @author Jeen Broekstra
 */
public class HTTPUpdate extends AbstractHTTPUpdate {

	private final HTTPRepositoryConnection httpCon;

	public HTTPUpdate(HTTPRepositoryConnection con, QueryLanguage ql, String queryString, String baseURI) {
		super(con.getSesameSession(), ql, queryString, baseURI);
		this.httpCon = con;
	}

	@Override
	public void execute()
		throws UpdateExecutionException
	{
		try {
			if (httpCon.useCompatibleMode()) {
				if (httpCon.isAutoCommit()) {
					// execute update immediately
					SparqlSession client = getHttpClient();
					try {
						client.sendUpdate(getQueryLanguage(), getQueryString(), getBaseURI(), dataset,
								includeInferred, getBindingsArray());
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
				return;
			}

			SparqlSession client = getHttpClient();
			try {
				httpCon.flushTransactionState(Action.UPDATE);
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
		catch (RepositoryException e) {
			throw new HTTPUpdateExecutionException(e.getMessage(), e);
		}

	}
}
