/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueExpr;

public class QueryModelNodeReplacer extends QueryModelVisitorBase<RuntimeException> {

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
