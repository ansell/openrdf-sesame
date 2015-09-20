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
package org.openrdf.sail.spin;

import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolverImpl;


public class SpinFederatedServiceResolver implements FederatedServiceResolver {

	static final String SPIN_SERVICE = "spin:/";

	private final FederatedServiceResolver delegate;

	public SpinFederatedServiceResolver() {
		this(new FederatedServiceResolverImpl());
	}

	public SpinFederatedServiceResolver(FederatedServiceResolver delegate) {
		this.delegate = delegate;
	}

	@Override
	public FederatedService getService(String serviceUrl)
		throws QueryEvaluationException
	{
		if(SPIN_SERVICE.equals(serviceUrl)) {
			return new SpinFederatedService();
		}
		else {
			return delegate.getService(serviceUrl);
		}
	}

}
