/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

import org.openrdf.query.algebra.QueryModelNodeBase;
import org.openrdf.query.algebra.QueryModelVisitor;

/**
 * An extension to {@link QueryModelNodeBase} for SQL query algebra.
 * 
 * @author James Leigh
 * 
 */
public abstract class RdbmsQueryModelNodeBase extends QueryModelNodeBase {

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		if (visitor instanceof RdbmsQueryModelVisitorBase) {
			visit((RdbmsQueryModelVisitorBase<X>)visitor);
		}
		else {
			visitor.meetOther(this);
		}
	}

	public abstract <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X;
}
