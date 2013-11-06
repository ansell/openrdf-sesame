/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package org.openrdf.sail.federation.optimizers;

import java.util.List;

import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.federation.algebra.NaryJoin;

/**
 * Supplies various query model statistics to the query engine/optimizer.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class EvaluationStatistics {

	protected CardinalityCalculator calculator;

	private final Object lock = new Object();

	public double getCardinality(TupleExpr expr) {
		synchronized (lock) {
			if (calculator == null) {
				calculator = createCardinalityCalculator();
			}
			expr.visit(calculator);
			return calculator.getCardinality();
		}
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
		public void meet(StatementPattern pattern) {
			cardinality = getCardinality(pattern);
		}

		protected double getCardinality(StatementPattern pattern) {
			List<Var> vars = pattern.getVarList();
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
		public void meetOther(QueryModelNode node) {
			if (node instanceof NaryJoin) {
				meetMultiJoin((NaryJoin)node);
			}
			else {
				super.meetOther(node);
			}
		}

		public void meetMultiJoin(NaryJoin node) {
			double cost = 1;
			for (TupleExpr arg : node.getArgs()) {
				arg.visit(this);
				cost *= this.cardinality;
			}
			cardinality = cost;
		}

		@Override
		public void meet(Join node) {
			double cost = 1;
			for (TupleExpr arg : new TupleExpr[] { node.getLeftArg(), // NOPMD
					node.getRightArg() })
			{
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
		protected void meetBinaryTupleOperator(BinaryTupleOperator node) {
			double cost = 0;
			for (TupleExpr arg : new TupleExpr[] { node.getLeftArg(), // NOPMD
					node.getRightArg() })
			{
				arg.visit(this);
				cost += cardinality;
			}
			cardinality = cost;
		}
	}
}
