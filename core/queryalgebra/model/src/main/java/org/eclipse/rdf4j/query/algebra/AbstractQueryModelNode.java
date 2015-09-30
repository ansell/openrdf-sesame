/*******************************************************************************
 * Copyright (c) 2015, Eclipse Foundation, Inc. and its licensors.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Eclipse Foundation, Inc. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission. 
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.eclipse.rdf4j.query.algebra;

import java.util.List;
import java.util.ListIterator;

import org.eclipse.rdf4j.query.algebra.helpers.QueryModelTreePrinter;

/**
 * Base implementation of {@link QueryModelNode}.
 */
public abstract class AbstractQueryModelNode implements QueryModelNode {

	/*-----------*
	 * Variables *
	 *-----------*/

	private static final long serialVersionUID = 3006199552086476178L;

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
	public AbstractQueryModelNode clone() {
		try {
			return (AbstractQueryModelNode)super.clone();
		}
		catch (CloneNotSupportedException e) {
			throw new RuntimeException("Query model nodes are required to be cloneable", e);
		}
	}

	protected <T extends QueryModelNode> boolean replaceNodeInList(List<T> list, QueryModelNode current, QueryModelNode replacement) {
		ListIterator<T> iter = list.listIterator();
		while (iter.hasNext()) {
			if (iter.next() == current) {
				iter.set((T)replacement);
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
