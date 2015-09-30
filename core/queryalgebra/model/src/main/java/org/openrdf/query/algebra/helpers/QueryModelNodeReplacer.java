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
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueExpr;

@Deprecated
public class QueryModelNodeReplacer extends AbstractQueryModelVisitor<RuntimeException> {

	private QueryModelNode former;

	private QueryModelNode replacement;

	public void replaceChildNode(QueryModelNode parent, QueryModelNode former, QueryModelNode replacement) {
		this.former = former;
		this.replacement = replacement;
		parent.visit(this);
	}

	public void replaceNode(QueryModelNode former, QueryModelNode replacement) {
		replaceChildNode(former.getParentNode(), former, replacement);
	}

	public void removeChildNode(QueryModelNode parent, QueryModelNode former) {
		replaceChildNode(parent, former, null);
	}

	public void removeNode(QueryModelNode former) {
		replaceChildNode(former.getParentNode(), former, null);
	}

	@Override
	public void meet(Filter node)
	{
		if (replacement == null) {
			replaceNode(node, node.getArg());
		}
		else if (replacement instanceof ValueExpr) {
			assert former == node.getCondition();
			node.setCondition((ValueExpr)replacement);
		}
		else {
			assert former == node.getArg();
			node.setArg((TupleExpr)replacement);
		}
	}

	@Override
	protected void meetBinaryTupleOperator(BinaryTupleOperator node)
	{
		if (node.getLeftArg() == former) {
			if (replacement == null) {
				replaceNode(node, node.getRightArg());
			}
			else {
				node.setLeftArg((TupleExpr)replacement);
			}
		}
		else {
			assert former == node.getRightArg();
			if (replacement == null) {
				replaceNode(node, node.getLeftArg());
			}
			else {
				node.setRightArg((TupleExpr)replacement);
			}
		}
	}

	@Override
	protected void meetBinaryValueOperator(BinaryValueOperator node)
	{
		if (former == node.getLeftArg()) {
			if (replacement == null) {
				replaceNode(node, node.getRightArg());
			}
			else {
				node.setLeftArg((ValueExpr)replacement);
			}
		}
		else {
			assert former == node.getRightArg();
			if (replacement == null) {
				replaceNode(node, node.getLeftArg());
			}
			else {
				node.setRightArg((ValueExpr)replacement);
			}
		}
	}

	@Override
	protected void meetUnaryTupleOperator(UnaryTupleOperator node)
	{
		assert former == node.getArg();
		if (replacement == null) {
			removeNode(node);
		}
		else {
			node.setArg((TupleExpr)replacement);
		}
	}

	@Override
	protected void meetUnaryValueOperator(UnaryValueOperator node)
	{
		assert former == node.getArg();
		if (replacement == null) {
			removeNode(node);
		}
		else {
			node.setArg((ValueExpr)replacement);
		}
	}

	@Override
	protected void meetNode(QueryModelNode node)
	{
		throw new IllegalArgumentException("Unhandled Node: " + node);
	}
}
