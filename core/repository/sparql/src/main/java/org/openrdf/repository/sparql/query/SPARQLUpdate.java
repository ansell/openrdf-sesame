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
package org.openrdf.repository.sparql.query;

import java.io.IOException;

import org.openrdf.http.client.SparqlSession;
import org.openrdf.http.client.query.AbstractHTTPUpdate;
import org.openrdf.http.protocol.UnauthorizedException;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryInterruptedException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.UpdateExecutionException;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

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
