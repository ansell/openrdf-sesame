/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Comparator;

import org.openrdf.model.Value;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.OptionalJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Compares the cost of StatementPattern based on number of unknown variables.
 * 
 * @author James Leigh <james@leighnet.ca>
 * 
 */
public class CostComparator extends QueryModelVisitorBase<RuntimeException> implements
		Comparator<TupleExpr> {
	protected int _cost;

	public int compare(TupleExpr p, TupleExpr q) {
		return costOf(p) - costOf(q);
	}

	public int costOf(TupleExpr expr) {
		expr.visit(this);
		return _cost;
	}

	@Override
	public void meet(EmptySet node) {
		_cost = 0;
	}

	@Override
	public void meet(Join node) {
		_cost = costOf(node.getLeftArg()) * costOf(node.getRightArg());
	}

	@Override
	public void meet(OptionalJoin node) {
		_cost = costOf(node.getLeftArg()) * costOf(node.getRightArg());
	}

	@Override
	public void meet(SingletonSet node) {
		_cost = 1;
	}

	@Override
	public void meet(StatementPattern node) {
		int cost = 0;
		if (getConstantValue(node.getSubjectVar()) == null)
			cost++;
		if (getConstantValue(node.getPredicateVar()) == null)
			cost++;
		if (getConstantValue(node.getObjectVar()) == null)
			cost++;
		if (getConstantValue(node.getContextVar()) == null)
			cost++;
		_cost = 1 << cost; // 2 ^ cost
	}

	@Override
	protected void meetBinaryTupleOperator(BinaryTupleOperator node) {
		_cost = costOf(node.getLeftArg()) + costOf(node.getRightArg());
	}

	@Override
	protected void meetNode(QueryModelNode node) {
		throw new IllegalArgumentException("Unhandled Node: " + node);
	}

	@Override
	protected void meetUnaryTupleOperator(UnaryTupleOperator node) {
		_cost = costOf(node.getArg());
	}

	protected Value getConstantValue(ValueExpr v) {
		if (v instanceof ValueConstant)
			return ((ValueConstant) v).getValue();
		if (v instanceof Var)
			return ((Var) v).getValue();
		return null;
	}
}