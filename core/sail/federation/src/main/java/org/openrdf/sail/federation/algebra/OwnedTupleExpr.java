/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.federation.algebra;

import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.repository.RepositoryConnection;


/**
 *
 * @author James Leigh
 */
public class OwnedTupleExpr extends UnaryTupleOperator {
	private RepositoryConnection owner;

	public OwnedTupleExpr(RepositoryConnection owner, TupleExpr arg) {
		super(arg);
		this.owner = owner;
	}

	public RepositoryConnection getOwner() {
		return owner;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meetOther(this);
	}

	@Override
	public String getSignature() {
		return this.getClass().getSimpleName() + " " + owner.toString();
	}

}
