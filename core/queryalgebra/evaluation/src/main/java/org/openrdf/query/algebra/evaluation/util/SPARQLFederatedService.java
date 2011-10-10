/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import java.util.Iterator;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.EmptyIteration;
import info.aduna.iteration.SingletonIteration;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.BooleanQuery;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.query.algebra.Service;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.SPARQLRepository;

/**
 * Federated Service wrapping the {@link SPARQLRepository} to communicate
 * with a SPARQL endpoint.
 * 
 * @author Andreas Schwarte
 */
public class SPARQLFederatedService implements FederatedService {

	
	protected final SPARQLRepository rep;
	protected RepositoryConnection conn = null;	
	
	/**
	 * 
	 * @param serviceUrl the serviceUrl use to initialize the inner {@link SPARQLRepository}
	 */
	public SPARQLFederatedService(String serviceUrl) {
		super();
		this.rep = new SPARQLRepository(serviceUrl);
	}



	/**
	 * Evaluate the provided sparqlQueryString at the initialized {@link SPARQLRepository}
	 * of this {@link FederatedService}. Dependent on the type (ASK/SELECT) different
	 * evaluation is necessary:
	 * 
	 * SELECT: insert bindings into SELECT query and evaluate
	 * ASK: insert bindings, send ask query and return final result
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			String sparqlQueryString, BindingSet bindings, String baseUri,
			QueryType type, Service service) throws QueryEvaluationException {

		try {
			// use a cache connection if possible 
			// (TODO add mechanism to unset/close connection)
			if (conn==null)
				conn = rep.getConnection();
			
			if (type==QueryType.SELECT) {
				
				TupleQuery query = conn.prepareTupleQuery(QueryLanguage.SPARQL, sparqlQueryString, baseUri);
				
				Iterator<Binding> bIter = bindings.iterator();
				while (bIter.hasNext()) {
					Binding b = bIter.next();
					if (service.getServiceVars().contains(b.getName()))
						query.setBinding(b.getName(), b.getValue());
				}					
				
				TupleQueryResult res = query.evaluate();
				
				return res;
				
			} else if (type==QueryType.ASK) {
				BooleanQuery query = conn.prepareBooleanQuery(QueryLanguage.SPARQL, sparqlQueryString, baseUri);
				
				Iterator<Binding> bIter = bindings.iterator();
				while (bIter.hasNext()) {
					Binding b = bIter.next();
					if (service.getServiceVars().contains(b.getName()))
						query.setBinding(b.getName(), b.getValue());
				}	
				
				boolean exists = query.evaluate();
				
				// check if triples are available (with inserted bindings)
				if (exists)
					return new SingletonIteration<BindingSet, QueryEvaluationException>(bindings);
				else
					return new EmptyIteration<BindingSet, QueryEvaluationException>();				
			} else
				throw new QueryEvaluationException("Unsupported QueryType: " + type.toString());
			
		} catch (MalformedQueryException e) {
			throw new QueryEvaluationException(e);
		} catch (RepositoryException e) {
			throw new QueryEvaluationException("SPARQLRepository for endpoint " + rep.toString() + " could not be initialized.", e);
		}
		
		
		
	}

}
