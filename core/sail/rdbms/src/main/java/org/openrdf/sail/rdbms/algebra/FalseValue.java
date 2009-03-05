/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.sail.rdbms.algebra.base.BooleanValue;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;

/**
 * Represents the value false in an SQL expression.
 * 
 * @author James Leigh
 */
public class FalseValue extends BooleanValue {

	public FalseValue() {
		super(false);
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
