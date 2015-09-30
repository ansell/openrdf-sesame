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
package org.eclipse.rdf4j.query.algebra.evaluation.federation;

import java.util.Collection;
import java.util.Set;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.SingletonIteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedService;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.SilentIteration;
import org.eclipse.rdf4j.query.impl.EmptyBindingSet;

/**
 * Fallback join handler, if the block join can not be performed, e.g. because
 * the BINDINGS clause is not supported by the endpoint. Gets a materialized
 * collection of bindings as input, and has to evaluate the join.
 * 
 * @author Andreas Schwarte
 */
public class ServiceFallbackIteration extends JoinExecutorBase<BindingSet> {

	protected final Service service;

	protected final Set<String> projectionVars;

	protected final FederatedService federatedService;

	protected final Collection<BindingSet> bindings;

	public ServiceFallbackIteration(Service service, Set<String> projectionVars, Collection<BindingSet> bindings,
			FederatedService federatedService)
		throws QueryEvaluationException
	{
		super(null, null, EmptyBindingSet.getInstance());
		this.service = service;
		this.projectionVars = projectionVars;
		this.bindings = bindings;
		this.federatedService = federatedService;
		run();
	}

	@Override
	protected void handleBindings()
		throws Exception
	{

		// NOTE: we do not have to care about SILENT services, as this
		// iteration by itself is wrapped in a silentiteration

		// handle each prepared query individually and add the result to this
		// iteration
		for (BindingSet b : bindings) {
			try {
				CloseableIteration<BindingSet, QueryEvaluationException> result = federatedService.select(
						service, projectionVars, b, service.getBaseURI());
				result = service.isSilent() ? new SilentIteration(result) : result;
				addResult(result);
			} 
			catch (QueryEvaluationException e) {
				// suppress exceptions if silent
				if (service.isSilent()) {
					addResult(new SingletonIteration<BindingSet, QueryEvaluationException>(b));
				} else {
					throw e;
				}
			}			
			catch (RuntimeException e) {
				// suppress special exceptions (e.g. UndeclaredThrowable with wrapped
				// QueryEval) if silent
				if (service.isSilent()) {
					addResult(new SingletonIteration<BindingSet, QueryEvaluationException>(b));
				}
				else {
					throw e;
				}
			}
		}

	}

}
