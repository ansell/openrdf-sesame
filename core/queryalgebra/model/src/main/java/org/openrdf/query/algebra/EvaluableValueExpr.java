package org.openrdf.query.algebra;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;

/**
 * Interface to support pipeline/stacked evaluation.
 * That is, each Sail in a stack can replace ValueExprs
 * with EvaluableValueExprs for the ValueExprs
 * it knows how to evaluate.
 * The base Sail can then evaluate the full query
 * by delegating the responsibility of evaluating any
 * EvaluableValueExprs to their implementations.
 */
public interface EvaluableValueExpr extends ValueExpr {
	Value evaluate(BindingSet bindings);
}
