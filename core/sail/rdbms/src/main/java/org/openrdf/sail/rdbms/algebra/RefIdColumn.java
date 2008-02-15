/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.query.algebra.Var;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.ValueColumnBase;

/**
 * Represents a variable's internal id value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class RefIdColumn extends ValueColumnBase {

	public RefIdColumn(ColumnVar var) {
		super(var);
	}

	public RefIdColumn(Var var) {
		super(var);
	}

	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}

	@Override
	public RefIdColumn clone() {
		return (RefIdColumn) super.clone();
	}
}
