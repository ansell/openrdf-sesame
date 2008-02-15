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
 * Represents a variable's Long URI value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class LongURIColumn extends ValueColumnBase {

	public LongURIColumn(Var var) {
		super(var);
	}

	public LongURIColumn(ColumnVar var) {
		super(var);
	}

	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}

}
