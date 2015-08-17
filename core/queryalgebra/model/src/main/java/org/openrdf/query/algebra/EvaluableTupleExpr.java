package org.openrdf.query.algebra;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;

/**
 * Interface to support pipeline/stacked evaluation.
 * That is, each Sail in a stack can replace TupleExprs
 * with EvaluableTupleExprs for the TupleExprs
 * it knows how to evaluate.
 * The base Sail can then evaluate the full query
 * by delegating the responsibility of evaluating any
 * EvaluableTupleExprs to their implementations.
 * 
 * This can be used to do backwards-chaining.
 */
public interface EvaluableTupleExpr extends TupleExpr {
	CloseableIteration<BindingSet, QueryEvaluationException> evaluate(BindingSet bindings);
}
