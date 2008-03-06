/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 * Copyright James Leigh (c) 2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra.evaluation.impl;

import java.util.Comparator;

import org.openrdf.query.BindingSet;
import org.openrdf.query.algebra.Join;
import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.query.algebra.evaluation.QueryOptimizer;
import org.openrdf.query.algebra.helpers.QueryModelNodeReplacer;
import org.openrdf.query.algebra.helpers.QueryModelVisitorBase;

/**
 * A query optimizer that re-orders nested Joins.
 * 
 * @author James Leigh <james@leighnet.ca>
 */
public class QueryJoinOptimizer extends QueryModelVisitorBase<RuntimeException> implements QueryOptimizer {

	private static enum Side {
		LEFT,
		RIGHT
	};

	private Comparator<TupleExpr> _costComparator;

	private QueryModelNodeReplacer _replacer = new QueryModelNodeReplacer();

	public QueryJoinOptimizer(Comparator<TupleExpr> comparator) {
		_costComparator = comparator;
	}

	/**
	 * Applies generally applicable optimizations: path expressions are sorted
	 * from more to less specific.
	 * 
	 * @param tupleExpr
	 * @return optimized TupleExpr
	 */
	public TupleExpr optimize(TupleExpr tupleExpr, BindingSet bindings) {
		tupleExpr.visit(this);
		return tupleExpr;
	}

	@Override
	public void meet(Join node)
	{
		if (!(node.getParentNode() instanceof Join)
				&& (node.getLeftArg() instanceof Join || node.getRightArg() instanceof Join))
		{
			QueryModelNode parent = node.getParentNode();
			TupleExpr replacement = _sort(node);
			if (replacement != node)
				_replacer.replaceChildNode(parent, node, replacement);
			super.meetNode(replacement);
		}
		else {
			super.meet(node);
		}
	}

	/*
	 * This method is copyright 2001 Simon Tatham. Permission is hereby granted,
	 * free of charge, to any person obtaining a copy of this software and
	 * associated documentation files (the "Software"), to deal in the Software
	 * without restriction, including without limitation the rights to use, copy,
	 * modify, merge, publish, distribute, sublicense, and/or sell copies of the
	 * Software, and to permit persons to whom the Software is furnished to do
	 * so, subject to the following conditions: The above copyright notice and
	 * this permission notice shall be included in all copies or substantial
	 * portions of the Software. THE SOFTWARE IS PROVIDED "AS IS", WITHOUT
	 * WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
	 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
	 * NONINFRINGEMENT. IN NO EVENT SHALL SIMON TATHAM BE LIABLE FOR ANY CLAIM,
	 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
	 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE
	 * USE OR OTHER DEALINGS IN THE SOFTWARE.
	 */
	private TupleExpr _sort(TupleExpr list) {
		int insize = 1;

		while (true) {
			TupleExpr p = list;
			TupleExpr tail = null;
			list = null;

			int nmerges = 0; /* count number of merges we do in this pass */

			while (p != null) {
				nmerges++; /* there exists a merge to be done */
				/* step `insize' places along from p */
				TupleExpr q = p;
				int psize = 0;
				for (int i = 0; i < insize; i++) {
					psize++;
					q = _getNext(q);
					if (q == null)
						break;
				}

				/* if q hasn't fallen off end, we have two lists to merge */
				int qsize = insize;

				/* now we have two lists; merge them */
				while (psize > 0 || (qsize > 0 && q != null)) {
					TupleExpr e;

					/* decide whether next element of merge comes from p or q */
					if (psize == 0) {
						/* p is empty; e must come from q. */
						e = q;
						q = _getNext(q);
						qsize--;
					}
					else if (qsize == 0 || q == null) {
						/* q is empty; e must come from p. */
						e = p;
						p = _getNext(p);
						psize--;
					}
					else if (_cmp(p, q) >= 0) {
						/*
						 * First element of p comes before (or same); e must come from
						 * p.
						 */
						e = p;
						p = _getNext(p);
						psize--;
					}
					else {
						/* First element of q comes before; e must come from q. */
						e = q;
						q = _getNext(q);
						qsize--;
					}

					/* add the next element to the merged list */
					if (tail != null) {
						TupleExpr join = _setNext(tail, e);
						// if tail is not a Join, replace it with a Join
						if (tail != join) {
							assert !(tail instanceof Join);
							assert join instanceof Join;
							if (list == tail)
								list = join;
							if (p == tail)
								p = join;
							if (q == tail)
								q = join;
							tail = join;
						}
					}
					else {
						list = e;
					}
					tail = e;
				}

				/* now p has stepped `insize' places along, and q has too */
				p = q;
			}
			_setNext(tail, null);

			/* If we have done only one merge, we're finished. */
			if (nmerges <= 1) /* allow for nmerges==0, the empty list case */
				return list;

			/* Otherwise repeat, merging lists twice the size */
			insize *= 2;
		}
	}

	private int _cmp(TupleExpr p, TupleExpr q) {
		TupleExpr v1 = _getValue(p);
		TupleExpr v2 = _getValue(q);
		return _costComparator.compare(v1, v2);
	}

	private TupleExpr _getNext(TupleExpr tupleExpr) {
		if (tupleExpr instanceof Join) {
			Join node = (Join)tupleExpr;
			switch (_joinOn(node)) {
				case LEFT:
					return node.getLeftArg();
				case RIGHT:
					return node.getRightArg();
				default:
					throw new AssertionError();
			}
		}
		else {
			return null;
		}
	}

	private TupleExpr _setNext(TupleExpr tupleExpr, TupleExpr next) {
		if (tupleExpr instanceof Join) {
			Join node = (Join)tupleExpr;
			switch (_joinOn(node)) {
				case LEFT:
					if (next == null) {
						_replacer.replaceNode(node, node.getRightArg());
					}
					else if (node.getLeftArg() != next) {
						node.setLeftArg(next);
					}
					break;
				case RIGHT:
					if (next == null) {
						_replacer.replaceNode(node, node.getLeftArg());
					}
					else if (node.getRightArg() != next) {
						node.setRightArg(next);
					}
					break;
				default:
					throw new AssertionError();
			}
		}
		else if (next != null) {
			QueryModelNode parentNode = tupleExpr.getParentNode();
			Join node = new Join(tupleExpr, next);
			_replacer.replaceChildNode(parentNode, tupleExpr, node);
			return node;
		}
		return tupleExpr;
	}

	private TupleExpr _getValue(TupleExpr tupleExpr) {
		if (tupleExpr instanceof Join) {
			Join node = (Join)tupleExpr;
			switch (_joinOn(node)) {
				case LEFT:
					return node.getRightArg();
				case RIGHT:
					return node.getLeftArg();
				default:
					throw new AssertionError();
			}
		}
		else {
			return tupleExpr;
		}
	}

	private Side _joinOn(Join node) {
		TupleExpr leftArg = node.getLeftArg();
		TupleExpr rightArg = node.getRightArg();
		if (rightArg instanceof Join && !(leftArg instanceof Join))
			return Side.RIGHT;
		return Side.LEFT;
	}
}
