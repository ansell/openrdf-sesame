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

import java.io.Serializable;

/**
 * Main interface for all query model nodes.
 */
public interface QueryModelNode extends Cloneable, Serializable {

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

	/**
	 * Replaces one of the child nodes with a new node.
	 * 
	 * @param current
	 *        The current child node.
	 * @param replacement
	 *        The new child node.
	 * @throws IllegalArgumentException
	 *         If <tt>current</tt> is not one of node's children.
	 * @throws ClassCastException
	 *         If <tt>replacement</tt> is of an incompatible type.
	 */
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement);

	/**
	 * Substitutes this node with a new node in the query model tree.
	 * 
	 * @param replacement
	 *        The new node.
	 * @throws IllegalStateException
	 *         If this node does not have a parent node.
	 * @throws ClassCastException
	 *         If <tt>replacement</tt> is of an incompatible type.
	 */
	public void replaceWith(QueryModelNode replacement);

	/**
	 * Returns <tt>true</tt> if this query model node and its children are
	 * recursively equal to <tt>o</tt> and its children.
	 */
	public boolean equals(Object o);

	/**
	 * Returns an indented print of the node tree, starting from this node.
	 */
	public String toString();

	/**
	 * Returns the signature of this query model node. Signatures normally
	 * include the node's name and any parameters, but not parent or child nodes.
	 * This method is used by {@link #toString()}.
	 * 
	 * @return The node's signature, e.g. <tt>SLICE (offset=10, limit=10)</tt>.
	 */
	public String getSignature();

	/**
	 * Returns a (deep) clone of this query model node. This method recursively
	 * clones the entire node tree, starting from this nodes.
	 * 
	 * @return A deep clone of this query model node.
	 */
	public QueryModelNode clone();
}
