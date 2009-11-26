/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2009.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.List;
import java.util.ListIterator;

import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;

/**
 * Base implementation of {@link QueryModelNode}.
 */
public abstract class QueryModelNodeBase implements QueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private QueryModelNode parent;

	/*---------*
	 * Methods *
	 *---------*/

	public QueryModelNode getParentNode() {
		return parent;
	}

	public void setParentNode(QueryModelNode parent) {
		this.parent = parent;
	}

	/**
	 * Dummy implementation of {@link QueryModelNode#visitChildren} that does
	 * nothing. Subclasses should override this method when they have child
	 * nodes.
	 */
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
	}

	/**
	 * Default implementation of
	 * {@link QueryModelNode#replaceChildNode(QueryModelNode, QueryModelNode)}
	 * that throws an {@link IllegalArgumentException} indicating that
	 * <tt>current</tt> is not a child node of this node.
	 */
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		throw new IllegalArgumentException("Node is not a child node: " + current);
	}

	/**
	 * Default implementation of
	 * {@link QueryModelNode#replaceWith(QueryModelNode)} that throws an
	 * {@link IllegalArgumentException} indicating that <tt>current</tt> is not a
	 * child node of this node.
	 */
	public void replaceWith(QueryModelNode replacement) {
		if (parent == null) {
			throw new IllegalStateException("Node has no parent");
		}

		parent.replaceChildNode(this, replacement);
	}

	/**
	 * Default implementation of {@link QueryModelNode#getSignature()} that
	 * prints the name of the node's class.
	 */
	public String getSignature() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		this.visit(treePrinter);
		return treePrinter.getTreeString();
	}

	@Override
	public QueryModelNodeBase clone() {
		try {
			return (QueryModelNodeBase)super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("Query model nodes are required to be cloneable", e);
		}
	}

	protected <T extends QueryModelNode> boolean replaceNodeInList(List<T> list, T current, T replacement) {
		ListIterator<T> iter = list.listIterator();
		while (iter.hasNext()) {
			if (iter.next() == current) {
				iter.set(replacement);
				replacement.setParentNode(this);
				return true;
			}
		}

		return false;
	}

	protected boolean nullEquals(Object o1, Object o2) {
		return o1 == o2 || o1 != null && o1.equals(o2);
	}
}
