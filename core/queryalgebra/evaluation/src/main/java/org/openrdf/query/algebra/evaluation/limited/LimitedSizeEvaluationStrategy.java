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
package org.openrdf.query.algebra.evaluation.limited;

import java.util.concurrent.atomic.AtomicLong;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.DelayedIteration;
import info.aduna.iteration.Iteration;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.Difference;
import org.openrdf.query.algebra.Distinct;
import org.openrdf.query.algebra.Intersection;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.Order;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.StatementPattern.Scope;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.federation.FederatedServiceResolver;
import org.openrdf.query.algebra.evaluation.federation.ServiceJoinIterator;
import org.openrdf.query.algebra.evaluation.impl.SimpleEvaluationStrategy;
import org.openrdf.query.algebra.evaluation.iterator.JoinIterator;
import org.openrdf.query.algebra.evaluation.iterator.ZeroLengthPathIteration;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizeDistinctIteration;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizeHashJoinIteration;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizeIntersectIteration;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizeOrderIteration;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizePathIterator;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizeSPARQLMinusIteration;
import org.openrdf.query.algebra.evaluation.limited.iterator.LimitedSizeZeroLengthPathIteration;
import org.openrdf.query.algebra.evaluation.util.OrderComparator;
import org.openrdf.query.algebra.evaluation.util.ValueComparator;
import org.openrdf.query.algebra.helpers.TupleExprs;

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
