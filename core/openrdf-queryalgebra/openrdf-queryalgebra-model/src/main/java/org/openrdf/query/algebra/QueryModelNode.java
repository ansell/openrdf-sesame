/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * Main interface for all query model nodes.
 */
public interface QueryModelNode {

	/**
	 * Visits this node. The node reports itself to the visitor with the proper
	 * runtime type.
	 */
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X;

	/**
	 * Visits the children of this node. The node calls
	 * {@link #visit(QueryModelVisitor)} on all of its child nodes.
	 */
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X;

	/**
	 * Gets the node's parent.
	 * 
	 * @return The parent node, if any.
	 */
	public QueryModelNode getParentNode();

	/**
	 * Sets the node's parent.
	 * 
	 * @param parent
	 *        The parent node for this node.
	 */
	public void setParentNode(QueryModelNode parent);
}
