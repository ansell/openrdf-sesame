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
package org.openrdf.spin;

import info.aduna.iteration.CloseableIteration;

import java.util.Set;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;


public class SpinFederatedService implements FederatedService {

	@Override
	public boolean ask(Service service, BindingSet bindings, String baseUri)
		throws QueryEvaluationException
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> select(Service service,
			Set<String> projectionVars, BindingSet bindings, String baseUri)
		throws QueryEvaluationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service service,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings, String baseUri)
		throws QueryEvaluationException
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isInitialized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize()
		throws QueryEvaluationException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown()
		throws QueryEvaluationException
	{
		// TODO Auto-generated method stub
		
	}

}
