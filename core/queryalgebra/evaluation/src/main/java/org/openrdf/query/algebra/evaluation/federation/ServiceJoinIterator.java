/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import java.util.ArrayList;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.iterator.CollectionIteration;

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
		
		// the number of bindings sent in a single subquery. 
		// if blockSize is set to 0, the entire input stream is used as block input
		// the block size effectively determines the number of remote requests
		int blockSize=15;	// TODO configurable block size
		
		if (blockSize>0) {
			while (leftIter.hasNext()) {
				
				ArrayList<BindingSet> blockBindings = new ArrayList<BindingSet>(blockSize);
				for (int i=0; i<blockSize; i++) {
					if (!leftIter.hasNext())
						break;
					blockBindings.add(leftIter.next());
				}
				CloseableIteration<BindingSet, QueryEvaluationException> materializedIter = 
							new CollectionIteration<BindingSet, QueryEvaluationException>(blockBindings);
				addResult(fs.evaluate(service, materializedIter, service.getBaseURI()));	
			}
		} else {
			// if blocksize is 0 (i.e. disabled) the entire iteration is used as block
			addResult(fs.evaluate(service, leftIter, service.getBaseURI()));	
		}
		
			
	}
}
