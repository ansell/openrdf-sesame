/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

/**
 * Abstract super class of all query model nodes.
 */
public abstract class QueryModelNode {

	/**
	 * Visits this node. The node reports itself to the visitor with the proper
	 * runtime type.
	 */
	public abstract void visit(QueryModelVisitor visitor);

	/**
	 * Visits the children of this node. The node calls
	 * {@link #visit(QueryModelVisitor)} on all of its child nodes. By default,
	 * this method does nothing. Subclasses should override this method when they
	 * have child nodes.
	 */
	public void visitChildren(QueryModelVisitor visitor) {
	}
}
