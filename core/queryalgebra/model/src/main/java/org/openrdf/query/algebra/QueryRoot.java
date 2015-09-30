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

	private QueryModelNode parent;

	public QueryRoot() {
		super();
	}

	public QueryRoot(TupleExpr tupleExpr) {
		super(tupleExpr);
	}

	@Override
	public void setParentNode(QueryModelNode parent) {
		if (parent instanceof QueryRoot) {
			this.parent = parent;
		}
		else {
			throw new UnsupportedOperationException("Not allowed to set a parent on a QueryRoot object");
		}
	}

	@Override
	public QueryModelNode getParentNode() {
		return this.parent;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof QueryRoot && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "QueryRoot".hashCode();
	}

	@Override
	public QueryRoot clone() {
		return (QueryRoot)super.clone();
	}
}
