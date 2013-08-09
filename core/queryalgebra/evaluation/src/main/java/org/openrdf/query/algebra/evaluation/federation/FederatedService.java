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

import java.util.Set;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.sparql.query.InsertBindingSetCursor;

/**
 * FederatedService to allow for customized evaluation of SERVICE expression. By
 * default {@link SPARQLFederatedService} is used.
 * 
 * @author Andreas Schwarte
 * @author James Leigh
 * @see SPARQLFederatedService
 */
public interface FederatedService {

	/**
	 * <p>
	 * Evaluate the provided SPARQL query at this federated service.
	 * </p>
	 * 
	 * <pre>
	 * Expected behavior: evaluate boolean query using the bindings as constraints
	 * </pre>
	 * 
	 * @param service
	 *        the reference to the service node, contains additional meta
	 *        information (vars, prefixes)
	 * @param bindings
	 *        the bindings serving as additional constraints
	 * @param baseUri
	 * @param type
	 *        the {@link QueryType}, either ASK or SELECT
	 * @return <code>true</code> if at least one result exists
	 * @throws QueryEvaluationException
	 */
	public boolean ask(Service service, BindingSet bindings, String baseUri)
		throws QueryEvaluationException;

	/**
	 * <p>
	 * Evaluate the provided SPARQL query at this federated service.
	 * </p>
	 * <p>
	 * <b>Important:</b> The original bindings need to be inserted into the
	 * result, e.g. via {@link InsertBindingSetCursor}.
	 * </p>
	 * 
	 * <pre>
	 * Expected behavior: evaluate the given SPARQL query using the bindings as constraints
	 * </pre>
	 * 
	 * @param service
	 *        the reference to the service node, contains additional meta
	 *        information (vars, prefixes)
	 * @param projectionVars
	 *        The variables with unknown value that should be projected from this
	 *        evaluation
	 * @param bindings
	 *        the bindings serving as additional constraints
	 * @param baseUri
	 * @return an iteration over the results of the query
	 * @throws QueryEvaluationException
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> select(Service service,
			Set<String> projectionVars, BindingSet bindings, String baseUri)
		throws QueryEvaluationException;

	/**
	 * Evaluate the provided SPARQL query at this federated service,
	 * possibilities for vectored evaluation.
	 * <p>
	 * <b>Contracts:</b>
	 * <ul>
	 * <li>The original bindings need to be inserted into the result, e.g. via
	 * {@link InsertBindingSetCursor}.</li>
	 * <li>SILENT service must be dealt with in the method</li>
	 * </ul>
	 * <p>
	 * Compare {@link SPARQLFederatedService} for a reference implementation
	 * </p>
	 * 
	 * @param service
	 *        the reference to the service node, contains information to
	 *        construct the query
	 * @param bindings
	 *        the bindings serving as additional constraints (for vectored
	 *        evaluation)
	 * @param baseUri
	 *        the baseUri
	 * @return the result of evaluating the query using bindings as constraints,
	 *         the original bindings need to be inserted into the results!
	 * @throws QueryEvaluationException
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service service,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings, String baseUri)
		throws QueryEvaluationException;

	/**
	 * Method to check if {@link #initialize()} had been called.
	 */
	public boolean isInitialized();

	/**
	 * Method to perform any initializations, invoked after construction.
	 */
	public void initialize()
		throws RepositoryException;

	/**
	 * Method to perform any shutDown code, invoked at unregistering.
	 */
	public void shutdown()
		throws RepositoryException;
}
