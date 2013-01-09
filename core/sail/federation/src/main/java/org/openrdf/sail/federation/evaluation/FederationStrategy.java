/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import info.aduna.iteration.CloseableIteration;
import info.aduna.iteration.UnionIteration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;

import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.algebra.evaluation.iterator.BadlyDesignedLeftJoinIterator;
import org.openrdf.sail.federation.algebra.NaryJoin;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;

/**
 * Evaluates Join, LeftJoin and Union in parallel and only evaluate if
 * {@link OwnedTupleExpr} is the given member.
 * 
 * @see ParallelJoinCursor
 * @see ParallelLeftJoinCursor
 * @author James Leigh
 */
public class FederationStrategy extends EvaluationStrategyImpl {

	private final Executor executor;

	public FederationStrategy(Executor executor, TripleSource tripleSource,
			Dataset dataset) {
		super(tripleSource, dataset);
		this.executor = executor;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			TupleExpr expr, BindingSet bindings)
			throws QueryEvaluationException {
		CloseableIteration<BindingSet, QueryEvaluationException> result;
		if (expr instanceof NaryJoin) {
			result = evaluate((NaryJoin) expr, bindings);
		} else if (expr instanceof OwnedTupleExpr) {
			result = evaluate((OwnedTupleExpr) expr, bindings);
		} else {
			result = super.evaluate(expr, bindings);
		}
		return result;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			Join join, BindingSet bindings) throws QueryEvaluationException {
		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluate(
				join.getLeftArg(), bindings);
		for (int i = 1, n = 2; i < n; i++) {
			result = new ParallelJoinCursor(this, result, join.getRightArg()); // NOPMD
			executor.execute((Runnable) result);
		}
		return result;
	}

	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			NaryJoin join, BindingSet bindings) throws QueryEvaluationException {
		assert join.getNumberOfArguments() > 0;
		CloseableIteration<BindingSet, QueryEvaluationException> result = evaluate(
				join.getArg(0), bindings);
		for (int i = 1, n = join.getNumberOfArguments(); i < n; i++) {
			result = new ParallelJoinCursor(this, result, join.getArg(i)); // NOPMD
			executor.execute((Runnable) result);
		}
		return result;
	}

	@Override
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			LeftJoin leftJoin, final BindingSet bindings)
			throws QueryEvaluationException {
		// Check whether optional join is "well designed" as defined in section
		// 4.2 of "Semantics and Complexity of SPARQL", 2006, Jorge Pérez et al.
		Set<String> boundVars = bindings.getBindingNames();
		Set<String> leftVars = leftJoin.getLeftArg().getBindingNames();
		Set<String> optionalVars = leftJoin.getRightArg().getBindingNames();

		final Set<String> problemVars = new HashSet<String>(boundVars);
		problemVars.retainAll(optionalVars);
		problemVars.removeAll(leftVars);

		CloseableIteration<BindingSet, QueryEvaluationException> result;
		if (problemVars.isEmpty()) {
			// left join is "well designed"
			result = new ParallelLeftJoinCursor(this, leftJoin, bindings);
			executor.execute((Runnable) result);
		} else {
			result = new BadlyDesignedLeftJoinIterator(this, leftJoin,
					bindings, problemVars);
		}
		return result;
	}

	@Override
	@SuppressWarnings("unchecked")
	public CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			Union union, BindingSet bindings) throws QueryEvaluationException {
		CloseableIteration<BindingSet, QueryEvaluationException>[] iters = new CloseableIteration[2];
		iters[0] = evaluate(union.getLeftArg(), bindings);
		iters[1] = evaluate(union.getRightArg(), bindings);
		return new UnionIteration<BindingSet, QueryEvaluationException>(iters);
	}

	private CloseableIteration<BindingSet, QueryEvaluationException> evaluate(
			OwnedTupleExpr expr, BindingSet bindings)
			throws QueryEvaluationException {
		CloseableIteration<BindingSet, QueryEvaluationException> result = expr
				.evaluate(dataset, bindings);
		if (result == null) {
			TripleSource source = new RepositoryTripleSource(expr.getOwner());
			EvaluationStrategy eval = new FederationStrategy(executor, source,
					dataset);
			result = eval.evaluate(expr.getArg(), bindings);
		}
		return result;
	}

}
