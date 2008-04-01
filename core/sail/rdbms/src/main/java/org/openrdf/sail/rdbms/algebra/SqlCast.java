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
 * The SQL IS CAST expression.
 * 
 * @author James Leigh
 * 
 */
public class SqlCast extends UnarySqlOperator {

	private int type;

	public SqlCast(SqlExpr arg, int type) {
		super(arg);
		this.type = type;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public int getType() {
		return type;
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " AS " + type;
	}

}
