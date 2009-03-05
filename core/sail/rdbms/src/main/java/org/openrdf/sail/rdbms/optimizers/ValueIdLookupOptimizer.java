/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.optimizers;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.QueryModel;
import org.openrdf.query.algebra.Var;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;
import org.openrdf.sail.rdbms.RdbmsValueFactory;

/**
 * Iterates through the query and converting the values into RDBMS values.
 * 
 * @author James Leigh
 */
public class ValueIdLookupOptimizer implements QueryOptimizer {

	RdbmsValueFactory vf;

	public ValueIdLookupOptimizer(RdbmsValueFactory vf) {
		super();
		this.vf = vf;
	}

	public void optimize(QueryModel query, BindingSet bindings) {
		query.visit(new VarVisitor());
	}

	protected class VarVisitor extends QueryModelVisitorBase<RuntimeException> {

		@Override
		public void meet(Var var) {
			if (var.hasValue()) {
				var.setValue(vf.asRdbmsValue(var.getValue()));
			}
		}
	}
}
