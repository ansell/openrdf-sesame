/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;
import org.openrdf.sail.rdbms.algebra.base.UnarySqlOperator;

/**
 * The SQL binary shift right (>>) expression.
 * 
 * @author James Leigh
 * 
 */
public class SqlShift extends UnarySqlOperator {
	private int shift;
	private int range;

	public SqlShift(SqlExpr arg, int shift, int range) {
		super(arg);
		this.shift = shift;
		this.range = range;
	}

	public int getRightShift() {
		return shift;
	}

	public int getRange() {
		return range;
	}

	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}

}
