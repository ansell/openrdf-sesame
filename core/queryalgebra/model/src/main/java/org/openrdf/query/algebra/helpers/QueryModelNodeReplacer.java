/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.Filter;
import org.openrdf.query.algebra.NaryTupleOperator;
import org.openrdf.query.algebra.NaryValueOperator;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.ValueExpr;

@Deprecated
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
	protected void meetNaryTupleOperator(NaryTupleOperator node)
	{
		if (node.getNumberOfArguments() == 1) {
			meetUnaryTupleOperator(node);
		} else if (node.getNumberOfArguments() == 2) {
			meetBinaryTupleOperator(node);
		} else {
			for (TupleExpr arg : node.getArgs()) {
				if (arg == former) {
					if (replacement == null) {
						node.removeChildNode(former);
					}
					else {
						node.replaceChildNode(former, replacement);
					}
				}
			}
		}
	}

	@Override
	protected void meetNaryValueOperator(NaryValueOperator node)
	{
		if (node.getNumberOfArguments() == 1) {
			meetUnaryValueOperator(node);
		} else if (node.getNumberOfArguments() == 2) {
			meetBinaryValueOperator(node);
		} else {
			for (ValueExpr arg : node.getArgs()) {
				if (arg == former) {
					if (replacement == null) {
						node.removeChildNode(former);
					}
					else {
						node.replaceChildNode(former, replacement);
					}
				}
			}
		}
	}

	private void meetBinaryTupleOperator(NaryTupleOperator node)
	{
		if (node.getArg(0) == former) {
			if (replacement == null) {
				replaceNode(node, node.getArg(1));
			}
			else {
				node.setArg(0, ((TupleExpr)replacement));
			}
		}
		else {
			assert former == node.getArg(1);
			if (replacement == null) {
				replaceNode(node, node.getArg(0));
			}
			else {
				node.setArg(1, ((TupleExpr)replacement));
			}
		}
	}

	private void meetBinaryValueOperator(NaryValueOperator node)
	{
		if (former == node.getArg(0)) {
			if (replacement == null) {
				replaceNode(node, node.getArg(1));
			}
			else {
				node.setArg(0, ((ValueExpr)replacement));
			}
		}
		else {
			assert former == node.getArg(1);
			if (replacement == null) {
				replaceNode(node, node.getArg(0));
			}
			else {
				node.setArg(1, ((ValueExpr)replacement));
			}
		}
	}

	private void meetUnaryTupleOperator(NaryTupleOperator node)
	{
		assert former == node.getArg(0);
		if (replacement == null) {
			removeNode(node);
		}
		else {
			node.setArg(0, ((TupleExpr)replacement));
		}
	}

	private void meetUnaryValueOperator(NaryValueOperator node)
	{
		assert former == node.getArg(0);
		if (replacement == null) {
			removeNode(node);
		}
		else {
			node.setArg(0, ((ValueExpr)replacement));
		}
	}

	@Override
	protected void meetNode(QueryModelNode node)
	{
		throw new IllegalArgumentException("Unhandled Node: " + node);
	}
}
