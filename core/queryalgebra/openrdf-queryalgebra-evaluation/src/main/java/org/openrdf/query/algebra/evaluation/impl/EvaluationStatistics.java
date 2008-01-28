/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2006-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Supplies various query model statistics to the query engine/optimizer.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class EvaluationStatistics {

	public double getCardinality(TupleExpr expr) {
		return getCardinality(expr, new HashSet<String>());
	}

	public double getCardinality(TupleExpr expr, Set<String> boundVars) {
		CardinalityCalculator cc = getCardinalityCalculator(boundVars);
		expr.visit(cc);
		return cc.getCardinality();
	}

	protected CardinalityCalculator getCardinalityCalculator(Set<String> boundVars) {
		return new CardinalityCalculator(boundVars);
	}

	/*-----------------------------------*
	 * Inner class CardinalityCalculator *
	 *-----------------------------------*/

	protected static class CardinalityCalculator extends QueryModelVisitorBase<RuntimeException> {

		protected Set<String> boundVars;

		protected double cardinality;

		public CardinalityCalculator(Set<String> boundVars) {
			this.boundVars = boundVars;
		}

		public double getCardinality() {
			return cardinality;
		}

		@Override
		public void meet(EmptySet node)
		{
			cardinality = 0;
		}

		@Override
		public void meet(SingletonSet node)
		{
			cardinality = 1;
		}

		@Override
		public void meet(StatementPattern sp)
		{
			cardinality = 1000.0;

			int constantVarCount = countConstantVars(sp);
			int boundVarCount = countBoundVars(sp);

			int sqrtFactor = 2 * boundVarCount + constantVarCount;

			if (sqrtFactor >= 2) {
				cardinality = Math.pow(cardinality, 1.0 / sqrtFactor);
			}
			
//			int unboundVars = 4 - countBoundVars(sp) - countConstantVars(sp);
//			cardinality = 1 << unboundVars; // 2 ^ unboundVars
		}

		protected int countConstantVars(StatementPattern sp) {
			int constantVarCount = 0;

			Var[] spVars = new Var[] {
					sp.getSubjectVar(),
					sp.getPredicateVar(),
					sp.getObjectVar(),
					sp.getContextVar() };

			for (Var var : spVars) {
				if (var != null && var.hasValue()) {
					constantVarCount++;
				}
			}

			return constantVarCount;
		}

		protected int countBoundVars(StatementPattern sp) {
			int boundVarCount = 0;

			Var[] spVars = new Var[] {
					sp.getSubjectVar(),
					sp.getPredicateVar(),
					sp.getObjectVar(),
					sp.getContextVar() };

			for (Var var : spVars) {
				if (var != null && this.boundVars.contains(var.getName())) {
					boundVarCount++;
				}
			}

			return boundVarCount;
		}

		@Override
		public void meet(Join node)
		{
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality *= leftArgCost;
		}

		@Override
		public void meet(LeftJoin node)
		{
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality *= leftArgCost;
		}

		@Override
		protected void meetBinaryTupleOperator(BinaryTupleOperator node)
		{
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality += leftArgCost;
		}

		@Override
		protected void meetUnaryTupleOperator(UnaryTupleOperator node)
		{
			node.getArg().visit(this);
		}

		@Override
		protected void meetNode(QueryModelNode node)
		{
			throw new IllegalArgumentException("Unhandled node type: " + node.getClass());
		}
	}
}