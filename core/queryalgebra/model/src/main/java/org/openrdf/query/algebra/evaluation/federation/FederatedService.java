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
package org.openrdf.query.algebra.evaluation.federation;

import java.util.Set;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;

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
	 * Evaluate the provided SPARQL ASK query at this federated service.
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
	 * @return <code>true</code> if at least one result exists
	 * @throws QueryEvaluationException
	 *         If there was an exception generated while evaluating the query.
	 */
	public boolean ask(Service service, BindingSet bindings, String baseUri)
		throws QueryEvaluationException;

	/**
	 * <p>
	 * Evaluate the provided SPARQL query at this federated service.
	 * </p>
	 * <p>
	 * <b>Important:</b> The original bindings need to be inserted into the
	 * result.
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
	 *         If there was an exception generated while evaluating the query.
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
	 * <li>The original bindings need to be inserted into the result</li>
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
	 *         If there was an exception generated while evaluating the query.
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
	 * 
	 * @throws QueryEvaluationException
	 *         If there was an exception generated while initializing the
	 *         service.
	 */
	public void initialize()
		throws QueryEvaluationException;

	/**
	 * Method to perform any shutDown code, invoked at unregistering.
	 * 
	 * @throws QueryEvaluationException
	 *         If there was an exception generated while shutting down the
	 *         service.
	 */
	public void shutdown()
		throws QueryEvaluationException;
}
