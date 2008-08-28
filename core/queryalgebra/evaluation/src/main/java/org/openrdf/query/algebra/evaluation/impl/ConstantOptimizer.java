/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.openrdf.StoreException;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BooleanLiteralImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.And;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.FunctionCall;
import org.openrdf.query.algebra.NaryValueOperator;
import org.openrdf.query.algebra.Or;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.EvaluationStrategy;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.evaluation.ValueExprEvaluationException;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.query.impl.EmptyBindingSet;

/**
 * A query optimizer that optimizes constant value expressions.
 * 
 * @author James Leigh
 * @author Arjohn Kampman
 */
public class ConstantOptimizer implements QueryOptimizer {

	protected final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected final EvaluationStrategy strategy;

	public ConstantOptimizer(EvaluationStrategy strategy) {
		this.strategy = strategy;
	}

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 * 
	 * @param tupleExpr
	 * @return optimized TupleExpr
	 * @throws StoreException
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings)
		throws StoreException
	{
		tupleExpr.visit(new ConstantVisitor());
	}

	protected class ConstantVisitor extends QueryModelVisitorBase<StoreException> {

		@Override
		public void meet(Or or)
			throws StoreException
		{
			or.visitChildren(this);

			try {
				for (ValueExpr arg : or.getArgs()) {
					if (isConstant(arg)) {
						if (strategy.isTrue(arg, EmptyBindingSet.getInstance())) {
							or.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
							return;
						} else {
							or.removeChildNode(arg);
						}
					}
				}
				if (or.getNumberOfArguments() == 0) {
					or.replaceWith(new ValueConstant(BooleanLiteralImpl.FALSE));
				} else if (or.getNumberOfArguments() == 1) {
					or.replaceWith(or.getArg(0));
				}
			}
			catch (ValueExprEvaluationException e) {
				logger.warn("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
		}

		@Override
		public void meet(And and)
			throws StoreException
		{
			and.visitChildren(this);

			try {
				for (ValueExpr arg : and.getArgs()) {
					if (isConstant(arg)) {
						boolean isTrue = strategy.isTrue(arg, EmptyBindingSet.getInstance());
						if (isTrue) {
							and.removeChildNode(arg);
						}
						else {
							and.replaceWith(new ValueConstant(BooleanLiteralImpl.FALSE));
							return;
						}
					}
				}
				if (and.getNumberOfArguments() == 0) {
					and.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
				} else if (and.getNumberOfArguments() == 1) {
					and.replaceWith(and.getArg(0));
				}
			}
			catch (ValueExprEvaluationException e) {
				logger.warn("Failed to evaluate And with some constant arguments", e);
			}
		}

		@Override
		protected void meetNaryValueOperator(NaryValueOperator naryValueOp)
			throws StoreException
		{
			super.meetNaryValueOperator(naryValueOp);

			for (ValueExpr arg : naryValueOp.getArgs()) {
				if (!isConstant(arg))
					return;
			}
			try {
				Value value = strategy.evaluate(naryValueOp, EmptyBindingSet.getInstance());
				naryValueOp.replaceWith(new ValueConstant(value));
			}
			catch (ValueExprEvaluationException e) {
				logger.warn("Failed to evaluate NaryValueOperator with a constant argument", e);
			}
		}

		@Override
		public void meet(FunctionCall functionCall)
			throws StoreException
		{
			super.meet(functionCall);

			List<ValueExpr> args = functionCall.getArgs();
			for (ValueExpr arg : args) {
				if (!isConstant(arg)) {
					return;
				}
			}

			// All arguments are constant

			try {
				Value value = strategy.evaluate(functionCall, EmptyBindingSet.getInstance());
				functionCall.replaceWith(new ValueConstant(value));
			}
			catch (ValueExprEvaluationException e) {
				logger.warn("Failed to evaluate BinaryValueOperator with two constant arguments", e);
			}
		}

		@Override
		public void meet(Bound bound)
			throws StoreException
		{
			super.meet(bound);

			if (bound.getArg().hasValue()) {
				// variable is always bound
				bound.replaceWith(new ValueConstant(BooleanLiteralImpl.TRUE));
			}
		}

		private boolean isConstant(ValueExpr expr) {
			return expr instanceof ValueConstant || expr instanceof Var && ((Var)expr).hasValue();
		}
	}
}
