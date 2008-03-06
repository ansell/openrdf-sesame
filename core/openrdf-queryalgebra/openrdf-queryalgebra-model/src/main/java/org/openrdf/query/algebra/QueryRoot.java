/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A semantics-less query model node that is used as the root of query model
 * trees. This is a placeholder that facilitates modifications to query model
 * trees, including the replacement of the actual (semantically relevant) root
 * node with another root node.
 * 
 * @author Arjohn Kampman
 */
public class QueryRoot extends UnaryTupleOperator {

	public QueryRoot() {
		super();
	}

	public QueryRoot(TupleExpr tupleExpr) {
		super(tupleExpr);
	}

	@Override
	public void setParentNode(QueryModelNode parent)
	{
		throw new UnsupportedOperationException("Not allowed to set a parent on a QueryRoot object");
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public QueryRoot clone() {
		return (QueryRoot)super.clone();
	}
}
