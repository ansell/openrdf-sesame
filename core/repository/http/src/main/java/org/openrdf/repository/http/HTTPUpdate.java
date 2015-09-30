/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
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
			if (httpCon.getRepository().useCompatibleMode()) {
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
