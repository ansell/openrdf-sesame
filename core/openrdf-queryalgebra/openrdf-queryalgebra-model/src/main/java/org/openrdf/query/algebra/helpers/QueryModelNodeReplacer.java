/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.helpers;

import org.openrdf.query.algebra.BinaryTupleOperator;
import org.openrdf.query.algebra.BinaryValueOperator;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.Selection;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.UnaryTupleOperator;
import org.openrdf.query.algebra.UnaryValueOperator;
import org.openrdf.query.algebra.ValueExpr;

public class QueryModelNodeReplacer extends QueryModelVisitorBase<RuntimeException> {

	private QueryModelNode _former;

	private QueryModelNode _replacement;

	public void replaceChildNode(QueryModelNode parent, QueryModelNode former,
			QueryModelNode replacement) {
		_former = former;
		_replacement = replacement;
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
	public void meet(Selection node) {
		if (_replacement == null) {
			replaceNode(node, node.getArg());
		} else if (_replacement instanceof ValueExpr) {
			assert _former == node.getCondition();
			node.setCondition((ValueExpr) _replacement);
		} else {
			assert _former == node.getArg();
			node.setArg((TupleExpr) _replacement);
		}
	}

	@Override
	protected void meetBinaryTupleOperator(BinaryTupleOperator node) {
		if (node.getLeftArg() == _former) {
			if (_replacement == null) {
				replaceNode(node, node.getRightArg());
			} else {
				node.setLeftArg((TupleExpr) _replacement);
			}
		} else {
			assert _former == node.getRightArg();
			if (_replacement == null) {
				replaceNode(node, node.getLeftArg());
			} else {
				node.setRightArg((TupleExpr) _replacement);
			}
		}
	}

	@Override
	protected void meetBinaryValueOperator(BinaryValueOperator node) {
		if (_former == node.getLeftArg()) {
			if (_replacement == null) {
				replaceNode(node, node.getRightArg());
			} else {
				node.setLeftArg((ValueExpr) _replacement);
			}
		} else {
			assert _former == node.getRightArg();
			if (_replacement == null) {
				replaceNode(node, node.getLeftArg());
			} else {
				node.setRightArg((ValueExpr) _replacement);
			}
		}
	}

	@Override
	protected void meetUnaryTupleOperator(UnaryTupleOperator node) {
		assert _former == node.getArg();
		if (_replacement == null) {
			removeNode(node);
		} else {
			node.setArg((TupleExpr) _replacement);
		}
	}

	@Override
	protected void meetUnaryValueOperator(UnaryValueOperator node) {
		assert _former == node.getArg();
		if (_replacement == null) {
			removeNode(node);
		} else {
			node.setArg((ValueExpr) _replacement);
		}
	}

	@Override
	protected void meetNode(QueryModelNode node) {
		throw new IllegalArgumentException("Unhandled Node: " + node);
	}
}
