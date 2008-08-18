/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2008.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.List;

import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.NaryTupleOperator;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Supplies various query model statistics to the query engine/optimizer.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class EvaluationStatistics {

	protected CardinalityCalculator cc;

	public synchronized double getCardinality(TupleExpr expr) {
		if (cc == null) {
			cc = createCardinalityCalculator();
		}

		expr.visit(cc);
		return cc.getCardinality();
	}

	protected CardinalityCalculator createCardinalityCalculator() {
		return new CardinalityCalculator();
	}

	/*-----------------------------------*
	 * Inner class CardinalityCalculator *
	 *-----------------------------------*/

	protected static class CardinalityCalculator extends QueryModelVisitorBase<RuntimeException> {

		protected double cardinality;

		public double getCardinality() {
			return cardinality;
		}

		@Override
		public void meet(EmptySet node) {
			cardinality = 0;
		}

		@Override
		public void meet(SingletonSet node) {
			cardinality = 1;
		}

		@Override
		public void meet(StatementPattern sp) {
			cardinality = getCardinality(sp);
		}

		protected double getCardinality(StatementPattern sp) {
			List<Var> vars = sp.getVarList();
			int constantVarCount = countConstantVars(vars);
			double unboundVarFactor = (double)(vars.size() - constantVarCount) / vars.size();
			return Math.pow(1000.0, unboundVarFactor);
		}

		protected int countConstantVars(Iterable<Var> vars) {
			int constantVarCount = 0;

			for (Var var : vars) {
				if (var.hasValue()) {
					constantVarCount++;
				}
			}

			return constantVarCount;
		}

		@Override
		public void meet(Join node) {
			double cost = 1;
			for (TupleExpr arg : node.getArgs()) {
				arg.visit(this);
				cost *= this.cardinality;
			}
			cardinality = cost;
		}

		@Override
		public void meet(LeftJoin node) {
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality *= leftArgCost;
		}

		@Override
		protected void meetNaryTupleOperator(NaryTupleOperator node) {
			double cost = 0;
			for (TupleExpr arg : node.getArgs()) {
				arg.visit(this);
				cost += cardinality;
			}
			cardinality = cost;
		}

		@Override
		protected void meetNode(QueryModelNode node) {
			throw new IllegalArgumentException("Unhandled node type: " + node.getClass());
		}
	}
}