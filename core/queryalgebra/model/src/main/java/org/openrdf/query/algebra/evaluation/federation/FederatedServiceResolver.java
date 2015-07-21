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

import org.openrdf.query.QueryEvaluationException;

/**
 * The {@link FederatedServiceResolver} is used to manage a set of
 * {@link FederatedService} instances, which are used to evaluate SERVICE
 * expressions for particular service Urls.
 * <p>
 * Lookup can be done via the serviceUrl using the method
 * {@link #getService(String)}.
 * 
 * @author Andreas Schwarte
 * @author James Leigh
 */
public interface FederatedServiceResolver {

	/**
	 * Retrieve the {@link FederatedService} registered for serviceUrl. If there
	 * is no service registered for serviceUrl, a new
	 * {@link SPARQLFederatedService} is created and registered.
	 * 
	 * @param serviceUrl
	 *        locator for the federation service
	 * @return the {@link FederatedService}, created fresh if necessary
	 * @throws QueryEvaluationException
	 *         If there was an exception generated while retrieving the service.
	 */
	FederatedService getService(String serviceUrl)
		throws QueryEvaluationException;

}
