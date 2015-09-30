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

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.evaluation.EvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedService;

/**
 * Iterator for efficient SERVICE evaluation (vectored). SERVICE is the right
 * handside argument of this join.
 * 
 * @author Andreas Schwarte
 */
public class ServiceJoinIterator extends JoinExecutorBase<BindingSet> {

	protected Service service;

	protected EvaluationStrategy strategy;

	/**
	 * Construct a service join iteration to use vectored evaluation. The
	 * constructor automatically starts evaluation.
	 * 
	 * @param leftIter
	 * @param service
	 * @param bindings
	 * @param strategy
	 * @throws QueryEvaluationException
	 */
	public ServiceJoinIterator(CloseableIteration<BindingSet, QueryEvaluationException> leftIter,
			Service service, BindingSet bindings, EvaluationStrategy strategy)
		throws QueryEvaluationException
	{
		super(leftIter, service, bindings);
		this.service = service;
		this.strategy = strategy;
		run();
	}

	@Override
	protected void handleBindings()
		throws Exception
	{
		Var serviceRef = service.getServiceRef();

		String serviceUri;
		if (serviceRef.hasValue())
			serviceUri = serviceRef.getValue().stringValue();
		else {
			// case 2: the service ref is not defined beforehand
			// => use a fallback to the naive evaluation.
			// exceptions occurring here must NOT be silenced!
			while (!closed && leftIter.hasNext()) {
				addResult(strategy.evaluate(service, leftIter.next()));
			}
			return;
		}

		// use vectored evaluation
		FederatedService fs = strategy.getService(serviceUri);
		addResult(fs.evaluate(service, leftIter, service.getBaseURI()));
	}
}
