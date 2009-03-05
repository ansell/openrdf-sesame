/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.helpers.QueryModelTreePrinter;
import org.openrdf.sail.rdbms.optimizers.SqlConstantOptimizer;

/**
 * An abstract binary sql operator with two arguments.
 * 
 * @author James Leigh
 */
public abstract class NarySqlOperator extends RdbmsQueryModelNodeBase implements SqlExpr {

	private List<SqlExpr> args = new ArrayList<SqlExpr>();

	public NarySqlOperator() {
		super();
	}

	public NarySqlOperator(SqlExpr... args) {
		setArgs(args);
	}

	public SqlExpr[] getArgs() {
		return args.toArray(new SqlExpr[args.size()]);
	}

	public void setArgs(SqlExpr... args) {
		assert args != null;
		assert args.length > 0;
		for (SqlExpr arg : args) {
			assert arg != null : "arg must not be null";
			arg.setParentNode(this);
		}
		this.args.clear();
		this.args.addAll(Arrays.asList(args));
	}

	public int getNumberOfArguments() {
		return args.size();
	}

	public SqlExpr getArg(int idx) {
		return args.get(idx);
	}

	public void setArg(int idx, SqlExpr arg) {
		arg.setParentNode(this);
		while (args.size() <= idx) {
			args.add(null);
		}
		args.set(idx, arg);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (SqlExpr arg : getArgs()) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			if (getArg(i) == current) {
				setArg(i, (SqlExpr)replacement);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

	public void removeChildNode(QueryModelNode current) {
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			if (getArg(i) == current) {
				args.remove(i);
				return;
			}
		}
		super.replaceChildNode(current, null);
	}

	@Override
	public NarySqlOperator clone() {
		NarySqlOperator clone = (NarySqlOperator)super.clone();
		clone.args = new ArrayList<SqlExpr>(getNumberOfArguments());
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			clone.setArg(i, getArg(i).clone());
		}
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		for (SqlExpr arg : getArgs()) {
			result = prime * result + ((arg == null) ? 0 : arg.hashCode());
		}
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
		final NarySqlOperator other = (NarySqlOperator)obj;
		return args.equals(other.args);
	}

	@Override
	public String toString() {
		QueryModelTreePrinter treePrinter = new QueryModelTreePrinter();
		NarySqlOperator clone = this.clone();
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
