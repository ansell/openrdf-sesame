/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.Bound;
import org.openrdf.query.algebra.EmptySet;
import org.openrdf.query.algebra.Extension;
import org.openrdf.query.algebra.ExtensionElem;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.LeftJoin;
import org.openrdf.query.algebra.ProjectionElem;
import org.openrdf.query.algebra.SameTerm;
import org.openrdf.query.algebra.StatementPattern;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.Union;
import org.openrdf.query.algebra.ValueConstant;
import org.openrdf.query.algebra.ValueExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that embeds {@link Filter}s with {@link SameTerm} operators
 * in statement patterns as much as possible. Operators like sameTerm(X, Y) are
 * processed by renaming X to Y (or vice versa). Operators like sameTerm(X,
 * <someURI>) are processed by assigning the URI to all occurring variables with
 * name X.
 * 
 * @author Arjohn Kampman
 * @author James Leigh
 */
public class SameTermFilterOptimizer implements QueryOptimizer {

	/**
	 * Applies generally applicable optimizations to the supplied query: variable
	 * assignments are inlined.
	 */
	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		tupleExpr.visit(new SameTermFilterVisitor());
	}

	protected class SameTermFilterVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Filter filter) {
			super.meet(filter);

			if (filter.getCondition() instanceof SameTerm) {
				// SameTerm applies to the filter's argument
				SameTerm sameTerm = (SameTerm)filter.getCondition();
				TupleExpr filterArg = filter.getArg();

				ValueExpr leftArg = sameTerm.getLeftArg();
				ValueExpr rightArg = sameTerm.getRightArg();

				// Verify that vars are bound by filterArg
				Set<String> bindingNames = filterArg.getBindingNames();

				if (leftArg instanceof Var && !bindingNames.contains(((Var)leftArg).getName())
						|| rightArg instanceof Var && !bindingNames.contains(((Var)rightArg).getName()))
				{
					// One or both var(s) are unbound, this expression will never
					// return any results
					filter.replaceWith(new EmptySet());
					return;
				}

				if (leftArg instanceof Var && rightArg instanceof Var) {
					// Rename rightArg to leftArg
					renameVar((Var)rightArg, (Var)leftArg, filter);
				}
				else if (leftArg instanceof Var && rightArg instanceof ValueConstant) {
					bindVar((Var)leftArg, (ValueConstant)rightArg, filter);
				}
				else if (rightArg instanceof Var && leftArg instanceof ValueConstant) {
					bindVar((Var)rightArg, (ValueConstant)leftArg, filter);
				}
			}
		}

		private void renameVar(Var oldVar, Var newVar, Filter filter) {
			filter.getArg().visit(new VarRenamer(oldVar.getName(), newVar.getName()));

			// TODO: skip this step if old variable name is not used
			// Replace SameTerm-filter with an Extension, the old variable name
			// might still be relevant to nodes higher in the tree
			Extension extension = new Extension(filter.getArg());
			extension.addElement(new ExtensionElem(new Var(newVar.getName()), oldVar.getName()));
			filter.replaceWith(extension);
		}

		private void bindVar(Var var, ValueConstant valueConstant, Filter filter) {
			filter.getArg().visit(new VarBinder(var.getName(), valueConstant.getValue()));

			// No need to keep the comparison, but we do need to make sure
			// that the variable is not null in case it comes from an
			// optional statement pattern. Replace the SameTerm constraint with a
			// Bound constraint.
			filter.setCondition(new Bound(var));

			// Check if the variable is used in a pattern outside of a left join.
			// If so, removed this filter condition
			filter.visit(new BoundOptimizer());
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

		@Override
		public void meet(ProjectionElem projElem)
			throws RuntimeException
		{
			if (projElem.getSourceName().equals(oldName)) {
				projElem.setSourceName(newName);
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

	protected class BoundOptimizer extends QueryModelVisitorBase<RuntimeException> {

		private boolean inSP;

		private List<Boolean> innerJoins = new ArrayList<Boolean>();

		private List<Var> vars = new ArrayList<Var>();

		@Override
		public void meet(Filter filter) {
			if (filter.getCondition() instanceof Bound) {
				Bound bound = (Bound)filter.getCondition();
				vars.add(bound.getArg());
				innerJoins.add(Boolean.FALSE);
				filter.getArg().visit(this);
				vars.remove(vars.size() - 1);
				if (innerJoins.remove(innerJoins.size() - 1)) {
					filter.replaceWith(filter.getArg());
				}
			}
			else {
				filter.visitChildren(this);
			}
		}

		@Override
		public void meet(Join join)
			throws RuntimeException
		{
			// search for statement patterns
			join.visitChildren(this);
		}

		@Override
		public void meet(LeftJoin leftJoin)
			throws RuntimeException
		{
			// search the left side, but not the optional right side
			leftJoin.getLeftArg().visit(this);
		}

		@Override
		public void meet(Union union)
			throws RuntimeException
		{
			List<Boolean> orig = innerJoins;

			// search left (independent of right)
			List<Boolean> left = innerJoins = new ArrayList<Boolean>(orig);
			union.getLeftArg().visit(this);

			// search right (independent of left)
			List<Boolean> right = innerJoins = new ArrayList<Boolean>(orig);
			union.getRightArg().visit(this);

			// compare results
			if (!left.equals(right)) {
				// not found on both sides
				innerJoins = orig;
			}
		}

		@Override
		protected void meetBinaryTupleOperator(BinaryTupleOperator node)
			throws RuntimeException
		{
			// don't search any more
		}

		@Override
		protected void meetUnaryTupleOperator(UnaryTupleOperator node)
			throws RuntimeException
		{
			// don't search any more
		}

		@Override
		public void meet(StatementPattern sp)
			throws RuntimeException
		{
			inSP = true;
			super.meet(sp);
			inSP = false;
		}

		@Override
		public void meet(Var var)
			throws RuntimeException
		{
			if (inSP && vars.contains(var)) {
				innerJoins.set(vars.indexOf(var), Boolean.TRUE);
			}
		}
	}
}
