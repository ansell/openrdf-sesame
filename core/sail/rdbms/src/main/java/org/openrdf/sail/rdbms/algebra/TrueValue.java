/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.sail.rdbms.algebra.base.BooleanValue;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;

/**
 * The boolean SQL expression of true.
 * 
 * @author James Leigh
 * 
 */
public class TrueValue extends BooleanValue {

	public TrueValue() {
		super(true);
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}
}
