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
 * Represents a variables language value in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class LanguageColumn extends ValueColumnBase {

	public LanguageColumn(Var var) {
		super(var);
	}

	public LanguageColumn(ColumnVar var) {
		super(var);
	}

	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}

}
