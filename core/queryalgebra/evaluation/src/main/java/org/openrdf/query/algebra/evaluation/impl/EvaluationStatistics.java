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
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Collection;

import org.openrdf.query.algebra.ArbitraryLengthPath;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BindingSetAssignment;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Service;
import org.openrdf.query.algebra.SingletonSet;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.ZeroLengthPath;
import org.openrdf.query.algebra.helpers.AbstractQueryModelVisitor;

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

	protected static class CardinalityCalculator extends AbstractQueryModelVisitor<RuntimeException> {

		private static double VAR_CARDINALITY = 10;

		private static double UNBOUND_SERVICE_CARDINALITY = 100000;

		protected double cardinality;

		public double getCardinality() {
			return cardinality;
		}

		@Override
		public void meet(EmptySet node) {
			// no binding sets
			cardinality = 0.0;
		}

		@Override
		public void meet(SingletonSet node) {
			// one empty binding set
			cardinality = 1.0;
		}

		@Override
		public void meet(BindingSetAssignment node) {
			// actual cardinality is node.getBindingSets().size() binding sets
			// but cost is cheap as we don't need to query the triple store
			// so effective cardinality is 1 or a very slowly increasing function of node.getBindingSets().size().
			cardinality = 1.0;
		}

		@Override
		public void meet(ZeroLengthPath node) {
			Var subjVar = node.getSubjectVar();
			Var objVar = node.getObjectVar();
			if((subjVar != null && subjVar.hasValue()) || (objVar != null && objVar.hasValue())) {
				// subj = obj
				cardinality = 1.0;
			}
			else {
				// actual cardinality = count(union(subjs, objs))
				// but cost is equivalent to ?s ?p ?o ?c (impl scans all statements)
				// so due to the lower actual cardinality we value it in preference to a fully unbound statement pattern.
				cardinality = getSubjectCardinality(subjVar) * getObjectCardinality(objVar) * getContextCardinality(node.getContextVar());
			}
		}

		@Override
		public void meet(ArbitraryLengthPath node) {

			// actual cardinality = count(union(subjs, objs))
			// but might require getting all statements
			// so due to the lower actual cardinality we value it in preference to a fully unbound statement pattern.
			cardinality = getSubjectCardinality(node.getSubjectVar()) * getObjectCardinality(node.getObjectVar()) * getContextCardinality(node.getContextVar());
		}

		@Override
		public void meet(Service node) {
			if (!node.getServiceRef().hasValue()) {
				// the URI is not available, may be computed in the course of the
				// query
				// => use high cost to order the SERVICE node late in the query plan
				cardinality = UNBOUND_SERVICE_CARDINALITY;
			}
			else {
				ServiceNodeAnalyzer serviceAnalyzer = new ServiceNodeAnalyzer();
				node.visitChildren(serviceAnalyzer);
				int count = serviceAnalyzer.getStatementCount();

				// more than one free variable in a single triple pattern
				if (count == 1 && node.getServiceVars().size() > 1) {
					cardinality = 100 + node.getServiceVars().size(); // TODO (should
																						// be higher
																						// than other
																						// simple
																						// stmts)
				}
				else {
					// only very selective statements should be better than this
					// => evaluate service expressions first
					cardinality = 1 + (node.getServiceVars().size() * 0.1);
				}
			}
		}

		@Override
		public void meet(StatementPattern sp) {
			cardinality = getCardinality(sp);
		}

		protected double getCardinality(StatementPattern sp) {
			return getSubjectCardinality(sp) * getPredicateCardinality(sp) * getObjectCardinality(sp)
					* getContextCardinality(sp);
		}

		/**
		 * Override this if you are able to determine the cardinality based not
		 * only on the subjectVar itself but also the other vars (e.g. the
		 * predicate value might determine a subject subset).
		 */
		protected double getSubjectCardinality(StatementPattern sp) {
			return getSubjectCardinality(sp.getSubjectVar());
		}

		protected double getSubjectCardinality(Var var) {
			return getCardinality(VAR_CARDINALITY, var);
		}

		/**
		 * Override this if you are able to determine the cardinality based not
		 * only on the predicateVar itself but also the other vars (e.g. the
		 * subject value might determine a predicate subset).
		 */
		protected double getPredicateCardinality(StatementPattern sp) {
			return getPredicateCardinality(sp.getPredicateVar());
		}

		protected double getPredicateCardinality(Var var) {
			return getCardinality(VAR_CARDINALITY, var);
		}

		/**
		 * Override this if you are able to determine the cardinality based not
		 * only on the objectVar itself but also the other vars (e.g. the
		 * predicate value might determine an object subset).
		 */
		protected double getObjectCardinality(StatementPattern sp) {
			return getObjectCardinality(sp.getObjectVar());
		}

		protected double getObjectCardinality(Var var) {
			return getCardinality(VAR_CARDINALITY, var);
		}

		/**
		 * Override this if you are able to determine the cardinality based not
		 * only on the contextVar itself but also the other vars (e.g. the subject
		 * value might determine a context subset).
		 */
		protected double getContextCardinality(StatementPattern sp) {
			return getContextCardinality(sp.getContextVar());
		}

		protected double getContextCardinality(Var var) {
			return getCardinality(VAR_CARDINALITY, var);
		}

		protected double getCardinality(double varCardinality, Var var) {
			return (var == null || var.hasValue()) ? 1.0 : varCardinality;
		}

		protected double getCardinality(double varCardinality, Collection<Var> vars) {
			int constantVarCount = countConstantVars(vars);
			double unboundVarFactor = vars.size() - constantVarCount;
			return Math.pow(varCardinality, unboundVarFactor);
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
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality *= leftArgCost;
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
			node.getLeftArg().visit(this);
			double leftArgCost = this.cardinality;

			node.getRightArg().visit(this);
			cardinality += leftArgCost;
		}

		@Override
		protected void meetUnaryTupleOperator(UnaryTupleOperator node) {
			node.getArg().visit(this);
		}

		@Override
		protected void meetNode(QueryModelNode node) {
			if (node instanceof ExternalSet) {
				meetExternalSet((ExternalSet)node);
			}
			else {
				throw new IllegalArgumentException("Unhandled node type: " + node.getClass());
			}
		}

		protected void meetExternalSet(ExternalSet node) {
			cardinality = node.cardinality();
		}
	}

	// count the number of triple patterns
	private static class ServiceNodeAnalyzer extends AbstractQueryModelVisitor<RuntimeException> {

		private int count = 0;

		public int getStatementCount() {
			return count;
		}

		@Override
		public void meet(StatementPattern node)
			throws RuntimeException
		{
			count++;
		}
	};
}
