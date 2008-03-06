/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that embeds {@link Filter}s with {@link SameTerm}
 * operators in statement patterns as much as possible. Operators like
 * sameTerm(X, Y) are processed by renaming X to Y (or vice versa). Operators
 * like sameTerm(X, <someURI>) are processed by assigning the URI to all
 * occurrnig variables with name X.
 * 
 * @author Arjohn Kampman
 */
public class SameTermFilterOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 * 
	 * @param tupleExpr
	 * @return optimized TupleExpr
	 * @throws QueryEvaluationException
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new SameTermFilterVisitor());
	}

	protected class SameTermFilterVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(SameTerm sameTerm) {
			super.meet(sameTerm);

			if (sameTerm.getParentNode() instanceof Filter) {
				// SameTerm applies to the filter's argument
				Filter filter = (Filter)sameTerm.getParentNode();
				TupleExpr filterArg = filter.getArg();

				ValueExpr leftArg = sameTerm.getLeftArg();
				ValueExpr rightArg = sameTerm.getRightArg();

				// Verify that vars are bound by filterArg
				Set<String> boundVars = filterArg.getBindingNames();

				if (leftArg instanceof Var && !boundVars.contains(((Var)leftArg).getName())
						|| rightArg instanceof Var && !boundVars.contains(((Var)rightArg).getName()))
				{
					// One or both var(s) are unbound, this expression will never
					// return any results
					filter.replaceWith(new EmptySet());
					return;
				}

				if (leftArg instanceof Var && rightArg instanceof Var) {
					// Rename rightArg to leftArg
					String leftVarName = ((Var)leftArg).getName();
					String rightVarName = ((Var)rightArg).getName();

					filterArg.visit(new VarRenamer(rightVarName, leftVarName));

					// Replace SameTerm-filter with an Extension
					Extension extension = new Extension(filterArg);
					extension.addElement(new ExtensionElem(new Var(leftVarName), rightVarName));
					filter.replaceWith(extension);
				}
				else if (leftArg instanceof Var && rightArg instanceof ValueConstant) {
					// Assign right value to left var
					String leftVarName = ((Var)leftArg).getName();
					Value rightValue = ((ValueConstant)rightArg).getValue();

					filterArg.visit(new VarBinder(leftVarName, rightValue));
				}
				else if (rightArg instanceof Var && leftArg instanceof ValueConstant) {
					// Assign left value to right var
					String rightVarName = ((Var)rightArg).getName();
					Value leftValue = ((ValueConstant)leftArg).getValue();

					filterArg.visit(new VarBinder(rightVarName, leftValue));
				}
			}
		}
	}

	protected class VarRenamer extends QueryModelVisitorBase<RuntimeException> {

		private String oldName;

		private String newName;

		public VarRenamer(String oldName, String newName) {
			this.oldName = oldName;
			this.newName = newName;
		}

		@Override
		public void meet(Var var) {
			if (var.getName().equals(oldName)) {
				var.setName(newName);
			}
		}
	}

	protected class VarBinder extends QueryModelVisitorBase<RuntimeException> {

		private String varName;

		private Value value;

		public VarBinder(String varName, Value value) {
			this.varName = varName;
			this.value = value;
		}

		@Override
		public void meet(Var var) {
			if (var.getName().equals(varName)) {
				var.setValue(value);
			}
		}
	}
}
