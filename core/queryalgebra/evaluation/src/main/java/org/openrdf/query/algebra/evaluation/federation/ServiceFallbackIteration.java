/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.federation;

import java.util.Collection;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.evaluation.federation.FederatedService.QueryType;
import org.openrdf.query.algebra.evaluation.iterator.SilentIteration;
import org.openrdf.query.impl.EmptyBindingSet;


/**
 * Fallback join handler, if the block join can not be performed, e.g. because the
 * BINDINGS clause is not supported by the endpoint. Gets a materialized collection
 * of bindings as input, and has to evaluate the join.
 * 
 * @author Andreas Schwarte
 */
public class ServiceFallbackIteration extends JoinExecutorBase<BindingSet> {

	
	protected final Service service;
	protected final String preparedQuery;
	protected final FederatedService federatedService;
	protected final Collection<BindingSet> bindings;
	
	/**
	 * @param leftIter
	 * @param rightArg
	 * @param bindings
	 * @throws QueryEvaluationException
	 */
	public ServiceFallbackIteration(Service service, String preparedQuery, Collection<BindingSet> bindings, 
			FederatedService federatedService)
			throws QueryEvaluationException {
		super(null, null, EmptyBindingSet.getInstance());
		this.service = service;
		this.preparedQuery = preparedQuery;
		this.bindings = bindings;
		this.federatedService = federatedService;
		run();
	}

	
	@Override
	protected void handleBindings() throws Exception {

		// NOTE: we do not have to care about SILENT services, as this
		// iteration by itself is wrapped in a silentiteration
		
		// handle each prepared query individually and add the result to this iteration
		for (BindingSet b : bindings) {					
			CloseableIteration<BindingSet, QueryEvaluationException> result = 
				federatedService.evaluate(preparedQuery, b, service.getBaseURI(), QueryType.SELECT, service);
			result = service.isSilent() ? new SilentIteration(result) : result;
			addResult(result);
		}
		
	}

}
