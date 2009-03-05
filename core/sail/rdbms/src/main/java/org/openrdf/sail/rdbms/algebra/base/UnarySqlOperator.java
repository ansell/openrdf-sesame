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
 * An SQL operator with one argument.
 * 
 * @author James Leigh
 */
public abstract class UnarySqlOperator extends RdbmsQueryModelNodeBase implements SqlExpr {

	private SqlExpr arg;

	public UnarySqlOperator() {
		super();
	}

	public UnarySqlOperator(SqlExpr arg) {
		super();
		setArg(arg);
	}

	public SqlExpr getArg() {
		return arg;
	}

	public void setArg(SqlExpr arg) {
		this.arg = arg;
		arg.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		arg.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (arg == current) {
			setArg((SqlExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public UnarySqlOperator clone() {
		UnarySqlOperator clone = (UnarySqlOperator)super.clone();
		clone.setArg(arg.clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((arg == null) ? 0 : arg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final UnarySqlOperator other = (UnarySqlOperator)obj;
		if (arg == null) {
			if (other.arg != null) {
				return false;
			}
		}
		else if (!arg.equals(other.arg)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		UnarySqlOperator clone = this.clone();
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
