/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;
import org.openrdf.sail.rdbms.optimizers.SqlConstantOptimizer;

/**
 * An abstract binary sql operator with two arguments.
 * 
 * @author James Leigh
 * 
 */
public abstract class BinarySqlOperator extends RdbmsQueryModelNodeBase implements SqlExpr {

	private SqlExpr leftArg;

	private SqlExpr rightArg;

	public BinarySqlOperator() {
		super();
	}

	public BinarySqlOperator(SqlExpr leftArg, SqlExpr rightArg) {
		super();
		setLeftArg(leftArg);
		setRightArg(rightArg);
	}

	public SqlExpr getLeftArg() {
		return leftArg;
	}

	public void setLeftArg(SqlExpr leftArg) {
		this.leftArg = leftArg;
		leftArg.setParentNode(this);
	}

	public SqlExpr getRightArg() {
		return rightArg;
	}

	public void setRightArg(SqlExpr rightArg) {
		this.rightArg = rightArg;
		rightArg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		leftArg.visit(visitor);
		rightArg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (leftArg == current) {
			setLeftArg((SqlExpr)replacement);
		}
		else if (rightArg == current) {
			setRightArg((SqlExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public BinarySqlOperator clone() {
		BinarySqlOperator clone = (BinarySqlOperator)super.clone();
		clone.setLeftArg(leftArg.clone());
		clone.setRightArg(rightArg.clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftArg == null) ? 0 : leftArg.hashCode());
		result = prime * result + ((rightArg == null) ? 0 : rightArg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final BinarySqlOperator other = (BinarySqlOperator)obj;
		if (leftArg == null) {
			if (other.leftArg != null)
				return false;
		}
		else if (!leftArg.equals(other.leftArg))
			return false;
		if (rightArg == null) {
			if (other.rightArg != null)
				return false;
		}
		else if (!rightArg.equals(other.rightArg))
			return false;
		return true;
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		BinarySqlOperator clone = this.clone();
		UnarySqlOperator parent = new UnarySqlOperator(clone) {

			@Override
			public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
				throws X
			{
				visitor.meetOther(this);
			}
		};
		new SqlConstantOptimizer().optimize(clone);
		parent.getArg().visit(treePrinter);
		return treePrinter.getTreeString();
	}
}
