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

import org.openrdf.repository.RepositoryException;

/**
 * The {@link FederatedServiceManager} is used to manage a set of
 * {@link FederatedService} instances, which are used to evaluate SERVICE
 * expressions for particular service Urls.
 * <p>
 * Lookup can be done via the serviceUrl using the method
 * {@link #getService(String)}.
 * <p>
 * The default behavior can be changed by extending from this class and setting
 * the implementation class via {@link #setInstance(FederatedServiceManager)}.
 * 
 * @author Andreas Schwarte
 * @author James Leigh
 */
public abstract class FederatedServiceManager {

	/*
	 * TODO maybe move to some other package ? 
	 */

	private static FederatedServiceManager instance = new FederatedServiceManagerImpl();

	public static synchronized FederatedServiceManager getInstance() {
		return instance;
	}

	public static synchronized void setInstance(FederatedServiceManager manager)
	{
		instance = manager;
	}

	protected FederatedServiceManager() {
		;
	}

	/**
	 * Retrieve the {@link FederatedService} registered for serviceUrl. If there
	 * is no service registered for serviceUrl, a new
	 * {@link SPARQLFederatedService} is created and registered.
	 * 
	 * @param serviceUrl
	 *        locator for the federation service
	 * @return the {@link FederatedService}, created fresh if necessary
	 * @throws RepositoryException
	 */
	public abstract FederatedService getService(String serviceUrl)
		throws RepositoryException;

	public abstract void shutDown();

}
