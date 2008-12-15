/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.evaluation;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.evaluation.QueryBindingSet;
import org.openrdf.query.algebra.evaluation.TripleSource;
import org.openrdf.query.algebra.evaluation.cursors.CompatibleBindingSetFilter;
import org.openrdf.query.algebra.evaluation.cursors.UnionCursor;
import org.openrdf.query.algebra.evaluation.impl.EvaluationStrategyImpl;
import org.openrdf.query.parser.TupleQueryModel;
import org.openrdf.results.Cursor;
import org.openrdf.sail.federation.algebra.OwnedTupleExpr;
import org.openrdf.store.StoreException;

/**
 * Evaluate Join, LeftJoin and Union in parallel and only evaluate {@linked
 * OwnedTupleExpr} is the given member.
 * 
 * @see ParallelJoinCursor
 * @see ParallelLeftJoinCursor
 * @author James Leigh
 */
public class FederationStrategy extends EvaluationStrategyImpl {

	private static Executor executor = Executors.newCachedThreadPool();

	public FederationStrategy(TripleSource tripleSource, QueryModel query) {
		super(tripleSource, query);
	}

	@Override
	public Cursor<BindingSet> evaluate(UnaryTupleOperator expr, BindingSet bindings)
		throws StoreException
	{
		if (expr instanceof OwnedTupleExpr) {
			return evaluate((OwnedTupleExpr)expr, bindings);
		}
		else {
			return super.evaluate(expr, bindings);
		}
	}

	@Override
	public Cursor<BindingSet> evaluate(Join join, BindingSet bindings)
		throws StoreException
	{
		assert join.getNumberOfArguments() > 0;
		Cursor<BindingSet> result;
		result = evaluate(join.getArg(0), bindings);
		for (int i = 1, n = join.getNumberOfArguments(); i < n; i++) {
			ParallelJoinCursor arg;
			arg = new ParallelJoinCursor(this, result, join.getArg(i), bindings);
			executor.execute(arg);
			result = arg;
		}
		return result;
	}

	@Override
	public Cursor<BindingSet> evaluate(LeftJoin leftJoin, BindingSet bindings)
		throws StoreException
	{
		// Check whether optional join is "well designed" as defined in section
		// 4.2 of "Semantics and Complexity of SPARQL", 2006, Jorge P�rez et al.
		Set<String> boundVars = bindings.getBindingNames();
		Set<String> leftVars = leftJoin.getLeftArg().getBindingNames();
		Set<String> optionalVars = leftJoin.getRightArg().getBindingNames();

		Set<String> problemVars = new HashSet<String>(boundVars);
		problemVars.retainAll(optionalVars);
		problemVars.removeAll(leftVars);

		if (problemVars.isEmpty()) {
			// left join is "well designed"
			ParallelLeftJoinCursor result;
			result = new ParallelLeftJoinCursor(this, leftJoin, bindings);
			executor.execute(result);
			return result;
		}
		else {
			QueryBindingSet filteredBindings = new QueryBindingSet(bindings);
			filteredBindings.removeAll(problemVars);
			Cursor<BindingSet> iter;

			ParallelLeftJoinCursor result;
			result = new ParallelLeftJoinCursor(this, leftJoin, filteredBindings);
			executor.execute(result);
			iter = result;
			iter = new CompatibleBindingSetFilter(iter, bindings);

			return iter;
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public Cursor<BindingSet> evaluate(Union union, BindingSet bindings)
		throws StoreException
	{
		int size = union.getNumberOfArguments();
		Cursor<BindingSet>[] iters = new Cursor[size];
		for (int i = 0; i < size; i++) {
			iters[i] = evaluate(union.getArg(i), bindings);
		}
		return new UnionCursor<BindingSet>(iters);
	}

	private Cursor<BindingSet> evaluate(OwnedTupleExpr expr, BindingSet bindings)
		throws StoreException
	{
		Cursor<BindingSet> result = expr.evaluate(dataset, bindings);
		if (result != null)
			return result;
		QueryModel query = createQueryModel(expr);
		TripleSource source = new RepositoryTripleSource(expr.getOwner());
		EvaluationStrategyImpl eval = new EvaluationStrategyImpl(source, query);
		return eval.evaluate(query, bindings);
	}

	private QueryModel createQueryModel(OwnedTupleExpr expr) {
		TupleQueryModel query = new TupleQueryModel(expr.getArg());
		if (dataset != null) {
			query.setDefaultGraphs(dataset.getDefaultGraphs());
			query.setNamedGraphs(dataset.getNamedGraphs());
		}
		return query;
	}

}
