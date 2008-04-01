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
 * The SQL LOWER function.
 * 
 * @author James Leigh
 * 
 */
public class SqlLowerCase extends UnarySqlOperator {

	public SqlLowerCase(SqlExpr arg) {
		super(arg);
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}