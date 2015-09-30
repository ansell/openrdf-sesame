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
package org.eclipse.rdf4j.query.algebra.evaluation;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.ValueExpr;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedService;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.SPARQLFederatedService;
import org.eclipse.rdf4j.repository.RepositoryException;

/**
 * Evaluates {@link TupleExpr}s and {@link ValueExpr}s.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public interface EvaluationStrategy extends FederatedServiceResolver {

	/**
	 * Retrieve the {@link FederatedService} registered for serviceUrl. If there
	 * is no service registered for serviceUrl, a new
	 * {@link SPARQLFederatedService} is created and registered.
	 * 
	 * @param serviceUrl
	 *        URL of the service.
	 * @return the {@link FederatedService} registered for the serviceUrl.
	 * @throws QueryEvaluationException
	 * @see org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver#getService(java.lang.String)
	 */
	public FederatedService getService(String serviceUrl)
		throws QueryEvaluationException;

	/**
	 * Evaluates the tuple expression against the supplied triple source with the
	 * specified set of variable bindings as input.
	 * 
	 * @param expr
	 *        The Service Expression to evaluate
	 * @param serviceUri
	 *        TODO
	 * @param bindings
	 *        The variables bindings iterator to use for evaluating the
	 *        expression, if applicable.
	 * @return A closeable iterator over all of variable binding sets that match
	 *         the tuple expression.
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service expr, String serviceUri,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings)
		throws QueryEvaluationException;

	/**
	 * Evaluates the tuple expression against the supplied triple source with the
	 * specified set of variable bindings as input.
	 * 
	 * @param expr
	 *        The Tuple Expression to evaluate
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return A closeable iterator over the variable binding sets that match the
	 *         tuple expression.
	 */
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
		throws QueryEvaluationException;

	/**
	 * Gets the value of this expression.
	 * 
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return The Value that this expression evaluates to, or <tt>null</tt> if
	 *         the expression could not be evaluated.
	 */
	public Value evaluate(ValueExpr expr, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException;

	/**
	 * Evaluates the boolean expression on the supplied TripleSource object.
	 * 
	 * @param bindings
	 *        The variables bindings to use for evaluating the expression, if
	 *        applicable.
	 * @return The result of the evaluation.
	 * @throws ValueExprEvaluationException
	 *         If the value expression could not be evaluated, for example when
	 *         comparing two incompatible operands. When thrown, the result of
	 *         the boolean expression is neither <tt>true</tt> nor <tt>false</tt>
	 *         , but unknown.
	 */
	public boolean isTrue(ValueExpr expr, BindingSet bindings)
		throws ValueExprEvaluationException, QueryEvaluationException;
}
