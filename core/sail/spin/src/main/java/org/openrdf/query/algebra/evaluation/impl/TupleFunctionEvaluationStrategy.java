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
package org.openrdf.query.algebra.evaluation.impl;

import java.util.List;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.TupleFunctionCall;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.federation.FederatedService;
import org.openrdf.query.algebra.evaluation.function.TupleFunction;
import org.openrdf.query.algebra.evaluation.function.TupleFunctionRegistry;


public class TupleFunctionEvaluationStrategy implements EvaluationStrategy {

	private final EvaluationStrategy delegate;
	private final TripleSource tripleSource;
	private final TupleFunctionRegistry tupleFuncRegistry;

	public TupleFunctionEvaluationStrategy(EvaluationStrategy delegate, TripleSource tripleSource) {
		this(delegate, tripleSource, TupleFunctionRegistry.getInstance());
	}

	public TupleFunctionEvaluationStrategy(EvaluationStrategy delegate, TripleSource tripleSource, TupleFunctionRegistry tupleFuncRegistry) {
		this.delegate = delegate;
		this.tripleSource = tripleSource;
		this.tupleFuncRegistry = tupleFuncRegistry;
	}

	@Override
	public FederatedService getService(String serviceUrl)
		throws QueryEvaluationException
	{
		return delegate.getService(serviceUrl);
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Service expr, String serviceUri,
			CloseableIteration<BindingSet, QueryEvaluationException> bindings)
		throws QueryEvaluationException
	{
		return delegate.evaluate(expr, serviceUri, bindings);
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleExpr expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		if(expr instanceof TupleFunctionCall) {
			return evaluate((TupleFunctionCall)expr, bindings);
		}
		else {
			return delegate.evaluate(expr, bindings);
		}
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleFunctionCall expr,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		TupleFunction func = tupleFuncRegistry.get(expr.getURI());

		if(func == null) {
			throw new QueryEvaluationException("Unknown function '" + expr.getURI() + "'");
		}

		List<ValueExpr> args = expr.getArgs();

		Value[] argValues = new Value[args.size()];

		for (int i = 0; i < args.size(); i++) {
			argValues[i] = evaluate(args.get(i), bindings);
		}

		return evaluate(func, expr.getResultVars(), bindings, tripleSource.getValueFactory(), argValues);
	}

	@Override
	public Value evaluate(ValueExpr expr, BindingSet bindings)
		throws QueryEvaluationException
	{
		return delegate.evaluate(expr, bindings);
	}

	@Override
	public boolean isTrue(ValueExpr expr, BindingSet bindings)
		throws QueryEvaluationException
	{
		return delegate.isTrue(expr, bindings);
	}



	public static CloseableIteration<BindingSet, QueryEvaluationException> evaluate(TupleFunction func,
			List<Var> resultVars, BindingSet bindings,
			ValueFactory valueFactory, Value... argValues)
		throws QueryEvaluationException
	{
		final CloseableIteration<? extends List<? extends Value>, QueryEvaluationException> iter = func.evaluate(valueFactory, argValues);
		return null; // TODO
	}
}
