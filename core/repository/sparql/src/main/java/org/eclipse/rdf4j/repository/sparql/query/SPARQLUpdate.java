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
package org.eclipse.rdf4j.repository.sparql.query;

import java.io.IOException;

import org.eclipse.rdf4j.http.client.SparqlSession;
import org.eclipse.rdf4j.http.client.query.AbstractHTTPUpdate;
import org.eclipse.rdf4j.http.protocol.UnauthorizedException;
import org.eclipse.rdf4j.query.MalformedQueryException;
import org.eclipse.rdf4j.query.QueryInterruptedException;
import org.eclipse.rdf4j.query.QueryLanguage;
import org.eclipse.rdf4j.query.UpdateExecutionException;
import org.eclipse.rdf4j.repository.RepositoryException;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;

/**
 * Update operation of the {@link SPARQLRepository}
 * 
 * @author Jeen Broekstra
 * @author Andreas Schwarte
 */
public class SPARQLUpdate extends AbstractHTTPUpdate {

	public SPARQLUpdate(SparqlSession httpClient, String baseURI, String queryString) {
		super(httpClient, QueryLanguage.SPARQL, queryString, baseURI);
	}

	@Override
	public void execute()
		throws UpdateExecutionException
	{

		try {
			// execute update immediately
			SparqlSession client = getHttpClient();
			try {
				client.sendUpdate(getQueryLanguage(), getQueryString(), getBaseURI(), dataset, includeInferred,
						getBindingsArray());
			}
			catch (UnauthorizedException e) {
				throw new UpdateExecutionException(e.getMessage(), e);
			}
			catch (QueryInterruptedException e) {
				throw new UpdateExecutionException(e.getMessage(), e);
			}
			catch (MalformedQueryException e) {
				throw new UpdateExecutionException(e.getMessage(), e);
			}
			catch (IOException e) {
				throw new UpdateExecutionException(e.getMessage(), e);
			}
		}
		catch (RepositoryException e) {
			throw new UpdateExecutionException(e.getMessage(), e);
		}

	}

	@Override
	public String getQueryString() {
		return QueryStringUtil.getQueryString(queryString, getBindings());
	}
}
