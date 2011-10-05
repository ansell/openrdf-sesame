/*
 * Copyright fluid Operations AG (http://www.fluidops.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.util;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;

/**
 * FederatedService to allow for customized evaluation of SERVICE expression.
 * By default {@link SPARQLFederatedService} is used.
 * 
 * @author Andreas Schwarte
 * 
 * @see SPARQLFederatedService
 */
public interface FederatedService {

	/**
	 * Query type
	 */
	public static enum QueryType { SELECT, ASK }
	
	
	/**
	 * Evaluate the provided SPARQL query at this federated service.<p>
	 * 
	 * <pre>
	 * Expected behavior:
	 * a) SELECT: evaluate the given SPARQL query using the bindings as constraints
	 * b) ASK: evaluate boolean query using the bindings as constraints
	 * 		- true => return new SingletonIteration(bindings);
	 *      - false => return new EmptyIteration<BindingSet, QueryEvaluationException>();
	 * </pre>
	 * 
	 * @param sparqlQueryString
	 * 				a SPARQL query (either SELECT or ASK, compare type parameter)
	 * @param bindings
	 * 				the bindings serving as additional constraints
	 * @param baseUri
	 * @param type
	 * 				the {@link QueryType}, either ASK or SELECT
	 * @param service
	 * 				the reference to the service node, contains additional meta information (vars, prefixes)
	 * 
	 * @return
	 * @throws QueryEvaluationException
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			String sparqlQueryString, BindingSet bindings, String baseUri,
			QueryType type, Service service) throws QueryEvaluationException;
}
