/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.Null;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelNodeReplacer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that in-lines variables and boolean expressions.
 * 
 * @author James Leigh <james@leighnet.ca>
 */
public class BooleanExprOptimizer extends QueryModelVisitorBase<RuntimeException> implements QueryOptimizer {

	/*-----------*
	 * Variables *
	 *-----------*/

	private BindingSet _bindings;

	private QueryModelNodeReplacer _replacer;

	private EvaluationStrategy _strategy;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public BooleanExprOptimizer(EvaluationStrategy strategy) {
		_strategy = strategy;
		_replacer = new QueryModelNodeReplacer();
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 * 
	 * @param tupleExpr
	 * @return optimized TupleExpr
	 * @throws QueryEvaluationException 
	 */
	public TupleExpr optimize(TupleExpr tupleExpr, BindingSet bindings) {
		_bindings = BindingsFinder.findBindings(tupleExpr, bindings);
		tupleExpr.visit(this);
		return tupleExpr;
	}

	@Override
	public void meet(Var node)
	{
		if (node.getValue() == null && _bindings.hasBinding(node.getName())) {
			Value value = _bindings.getValue(node.getName());
			node.setValue(value);
		}
	}

	@Override
	protected void meetBinaryValueOperator(BinaryValueOperator node)
	{
		super.meetBinaryValueOperator(node);
		ValueExpr left = node.getLeftArg();
		ValueExpr right = node.getRightArg();
		if (isConstant(left) && isConstant(right)) {
			_replacer.replaceNode(node, new ValueConstant(_strategy.getValue(node, _bindings)));
		}
	}

	@Override
	protected void meetUnaryValueOperator(UnaryValueOperator node)
	{
		super.meetUnaryValueOperator(node);
		ValueExpr arg = node.getArg();
		if (isConstant(arg)) {
			replaceWithConstant(node);
		}
	}

	private void replaceWithConstant(UnaryValueOperator node) {
		Value value = _strategy.getValue(node, _bindings);
		ValueConstant constant = new ValueConstant(value);
		_replacer.replaceNode(node, constant);
	}

	private boolean isConstant(ValueExpr expr) {
		return expr instanceof ValueConstant || expr instanceof Null || expr instanceof Var
				&& ((Var)expr).getValue() != null;
	}
}
