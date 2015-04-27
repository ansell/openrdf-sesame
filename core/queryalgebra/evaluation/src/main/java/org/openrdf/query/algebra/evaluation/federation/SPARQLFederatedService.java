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
package org.openrdf.query.algebra.evaluation.federation;

import org.openrdf.http.client.SesameClient;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 * Federated Service wrapping the {@link SPARQLRepository} to communicate with a
 * SPARQL endpoint.
 * 
 * @author Andreas Schwarte
 */
public class SPARQLFederatedService extends RepositoryFederatedService {

	private static SPARQLRepository createSPARQLRepository(String serviceUrl, SesameClient client)
	{
		SPARQLRepository rep = new SPARQLRepository(serviceUrl) {

			@Override
			protected void shutDownInternal()
				throws RepositoryException
			{
				setSesameClient(null);
				super.shutDownInternal();
			}
		};
		rep.setSesameClient(client);
		return rep;
	}

	/**
	 * @param serviceUrl
	 *        the serviceUrl use to initialize the inner {@link SPARQLRepository}
	 */
	public SPARQLFederatedService(String serviceUrl, SesameClient client) {
		super(createSPARQLRepository(serviceUrl, client));
	}
}
