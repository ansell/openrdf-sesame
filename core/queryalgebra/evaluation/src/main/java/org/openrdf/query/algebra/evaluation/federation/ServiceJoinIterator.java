/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;

/**
 * Iterator for efficient SERVICE evaluation (vectored).
 * 
 * SERVICE is the right handside argument of this join.
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
	 * @param rightArg
	 * @param bindings
	 * @throws QueryEvaluationException
	 */
	public ServiceJoinIterator(
			CloseableIteration<BindingSet, QueryEvaluationException> leftIter,
			Service service, BindingSet bindings, EvaluationStrategy strategy)
			throws QueryEvaluationException {
		super(leftIter, service, bindings);
		this.service = service;
		this.strategy = strategy;
		run();
	}	
	
	@Override
	protected void handleBindings() throws Exception {
		Var serviceRef = service.getServiceRef();

		String serviceUri;
		if (serviceRef.hasValue())
			serviceUri = serviceRef.getValue().stringValue();
		else {
			// case 2: the service ref is not defined beforehand 
			//   => use a fallback to the naive evaluation.
			// exceptions occurring here must NOT be silenced!
			while (!closed && leftIter.hasNext()) {
				addResult( strategy.evaluate( service, leftIter.next()) );
			}			
			return;			
		}
		
		// use vectored evaluation
		FederatedService fs = FederatedServiceManager.getInstance().getService(serviceUri);
		
		// TODO do in a loop for each block
		// TODO configurable block size
		addResult(fs.evaluate(service, leftIter, service.getBaseURI()));		
	}
}
