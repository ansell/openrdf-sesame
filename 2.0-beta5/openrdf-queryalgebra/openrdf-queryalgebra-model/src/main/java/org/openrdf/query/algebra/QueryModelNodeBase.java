/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Base implementation of {@link QueryModelNode}.
 */
public abstract class QueryModelNodeBase implements QueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private QueryModelNode _parent;

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Dummy implementation of {@link QueryModelNode#visitChildren} that does
	 * nothing. Subclasses should override this method when they have child
	 * nodes.
	 * 
	 * @throws X
	 */
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
	}

	public QueryModelNode getParentNode() {
		return _parent;
	}

	public void setParentNode(QueryModelNode parent) {
		_parent = parent;
	}
}
