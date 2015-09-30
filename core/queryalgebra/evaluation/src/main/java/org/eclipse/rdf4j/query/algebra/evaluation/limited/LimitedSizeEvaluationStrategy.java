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
package org.eclipse.rdf4j.query.algebra.evaluation.limited;

import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.rdf4j.common.iteration.CloseableIteration;
import org.eclipse.rdf4j.common.iteration.DelayedIteration;
import org.eclipse.rdf4j.common.iteration.Iteration;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.Dataset;
import org.eclipse.rdf4j.query.QueryEvaluationException;
import org.eclipse.rdf4j.query.algebra.ArbitraryLengthPath;
import org.eclipse.rdf4j.query.algebra.Difference;
import org.eclipse.rdf4j.query.algebra.Distinct;
import org.eclipse.rdf4j.query.algebra.Intersection;
import org.eclipse.rdf4j.query.algebra.Join;
import org.eclipse.rdf4j.query.algebra.Order;
import org.eclipse.rdf4j.query.algebra.Service;
import org.eclipse.rdf4j.query.algebra.TupleExpr;
import org.eclipse.rdf4j.query.algebra.Var;
import org.eclipse.rdf4j.query.algebra.StatementPattern.Scope;
import org.eclipse.rdf4j.query.algebra.evaluation.TripleSource;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.eclipse.rdf4j.query.algebra.evaluation.federation.ServiceJoinIterator;
import org.eclipse.rdf4j.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.JoinIterator;
import org.eclipse.rdf4j.query.algebra.evaluation.iterator.ZeroLengthPathIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizeDistinctIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizeHashJoinIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizeIntersectIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizeOrderIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizePathIterator;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizeSPARQLMinusIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.limited.iterator.LimitedSizeZeroLengthPathIteration;
import org.eclipse.rdf4j.query.algebra.evaluation.util.OrderComparator;
import org.eclipse.rdf4j.query.algebra.evaluation.util.ValueComparator;
import org.eclipse.rdf4j.query.algebra.helpers.TupleExprs;

/**
 * @author Jerven Bolleman, SIB Swiss Institute of Bioinformatics
 */
public class LimitedSizeEvaluationStrategy extends SimpleEvaluationStrategy {

	private final AtomicLong used = new AtomicLong();

	private long maxSize;

	/**
	 * @param tripleSource
	 */
	public LimitedSizeEvaluationStrategy(TripleSource tripleSource, long maxSize,
			FederatedServiceResolver serviceManager)
	{
		super(tripleSource, serviceManager);
		this.maxSize = maxSize;
	}

	/**
	 * @param tripleSource
	 * @param dataset
	 * @param maxCollectionsSize
	 */
	public LimitedSizeEvaluationStrategy(TripleSource tripleSource, Dataset dataset,
			int maxCollectionsSize, FederatedServiceResolver serviceManager)
	{
		super(tripleSource, dataset, serviceManager);
		this.maxSize = maxCollectionsSize;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Distinct distinct,
			BindingSet bindings)
		throws QueryEvaluationException
	{
		return new LimitedSizeDistinctIteration(evaluate(distinct.getArg(), bindings), used, maxSize);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Difference difference,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(difference.getRightArg(), bindings);
			}
		};

		return new LimitedSizeSPARQLMinusIteration(leftArg, rightArg, used, maxSize);
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(final Intersection intersection,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		Iteration<BindingSet, QueryEvaluationException> leftArg, rightArg;

		leftArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getLeftArg(), bindings);
			}
		};

		rightArg = new DelayedIteration<BindingSet, QueryEvaluationException>() {

			@Override
			protected Iteration<BindingSet, QueryEvaluationException> createIteration()
				throws QueryEvaluationException
			{
				return evaluate(intersection.getRightArg(), bindings);
			}
		};

		return new LimitedSizeIntersectIteration(leftArg, rightArg, used, maxSize);
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Join join, BindingSet bindings)
		throws QueryEvaluationException
	{
		// efficient computation of a SERVICE join using vectored evaluation
		// TODO maybe we can create a ServiceJoin node already in the parser?
		if (join.getRightArg() instanceof Service) {
			CloseableIteration<BindingSet, QueryEvaluationException> leftIter = evaluate(join.getLeftArg(),
					bindings);
			return new ServiceJoinIterator(leftIter, (Service)join.getRightArg(), bindings, this);
		}

		if (TupleExprs.containsProjection(join.getRightArg())) {
			return new LimitedSizeHashJoinIteration(this, join, bindings, used, maxSize);
		}
		else {
			return new JoinIterator(this, join, bindings);
		}
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(ArbitraryLengthPath alp,
			final BindingSet bindings)
		throws QueryEvaluationException
	{
		final Scope scope = alp.getScope();
		final Var subjectVar = alp.getSubjectVar();
		final TupleExpr pathExpression = alp.getPathExpression();
		final Var objVar = alp.getObjectVar();
		final Var contextVar = alp.getContextVar();
		final long minLength = alp.getMinLength();

		return new LimitedSizePathIterator(this, scope, subjectVar, pathExpression, objVar, contextVar, minLength, bindings, used, maxSize);
	}
	@Override
	protected ZeroLengthPathIteration getZeroLengthPathIterator(BindingSet bindings, Var subjectVar,
			Var objVar, Var contextVar, Value subj, Value obj)
	{
		return new LimitedSizeZeroLengthPathIteration(this, subjectVar, objVar, subj, obj, contextVar,
				bindings, used, maxSize);
	}
	
	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(Order node, BindingSet bindings)
			throws QueryEvaluationException
		{
			ValueComparator vcmp = new ValueComparator();
			OrderComparator cmp = new OrderComparator(this, node, vcmp);
			boolean reduced = isReducedOrDistinct(node);
			long limit = getLimit(node);
			return new LimitedSizeOrderIteration(evaluate(node.getArg(), bindings), cmp, limit, reduced, used, maxSize);
		}
}
