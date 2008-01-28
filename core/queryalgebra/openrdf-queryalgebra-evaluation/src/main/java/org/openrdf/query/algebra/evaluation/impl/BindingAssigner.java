/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.Dataset;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * Assigns values to variables based on a supplied set of bindings.
 * 
 * @author Arjohn Kampman
 */
public class BindingAssigner implements QueryOptimizer {

	public void optimize(TupleExpr tupleExpr, Dataset dataset, BindingSet bindings) {
		if (bindings.size() > 0) {
			tupleExpr.visit(new VarVisitor(bindings));
		}
	}

	protected class VarVisitor extends QueryModelVisitorBase<RuntimeException> {

		protected BindingSet bindings;

		public VarVisitor(BindingSet bindings) {
			this.bindings = bindings;
		}

		@Override
		public void meet(Var var) {
			if (!var.hasValue() && bindings.hasBinding(var.getName())) {
				Value value = bindings.getValue(var.getName());
				var.setValue(value);
			}
		}
	}
}
