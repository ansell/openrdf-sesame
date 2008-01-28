/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser.serql;

import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;

/**
 * @author Arjohn Kampman
 */
class OptionalTupleExpr {

	private final TupleExpr tupleExpr;

	private final ValueExpr constraint;

	public OptionalTupleExpr(TupleExpr tupleExpr) {
		this(tupleExpr, null);
	}

	public OptionalTupleExpr(TupleExpr tupleExpr, ValueExpr constraint) {
		this.tupleExpr = tupleExpr;
		this.constraint = constraint;
	}

	public TupleExpr getTupleExpr() {
		return tupleExpr;
	}

	public ValueExpr getConstraint() {
		return constraint;
	}

	public boolean hasConstraint() {
		return constraint != null;
	}
}
