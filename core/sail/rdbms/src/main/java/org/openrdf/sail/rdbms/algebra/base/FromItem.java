/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra.base;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.rdbms.algebra.ColumnVar;

/**
 * An item in the SQL from clause.
 * 
 * @author James Leigh
 * 
 */
public abstract class FromItem extends RdbmsQueryModelNodeBase {

	private String alias;

	private boolean left;

	private List<FromItem> joins = new ArrayList<FromItem>();

	private List<SqlExpr> filters = new ArrayList<SqlExpr>();

	public FromItem(String alias) {
		super();
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

	public boolean isLeft() {
		return left;
	}

	public void setLeft(boolean left) {
		this.left = left;
	}

	public List<SqlExpr> getFilters() {
		return filters;
	}

	public void addFilter(SqlExpr filter) {
		this.filters.add(filter);
		filter.setParentNode(this);
	}

	public List<FromItem> getJoins() {
		return joins;
	}

	public ColumnVar getVarForChildren(String name) {
		for (FromItem join : joins) {
			ColumnVar var = join.getVar(name);
			if (var != null)
				return var;
		}
		return null;
	}

	public ColumnVar getVar(String name) {
		return getVarForChildren(name);
	}

	public void addJoin(FromItem join) {
		joins.add(join);
		joinAdded(join);
	}

	public void addJoinBefore(FromItem valueJoin, FromItem join) {
		for (int i = 0, n = joins.size(); i < n; i++) {
			if (joins.get(i) == join) {
				joins.add(i, valueJoin);
				joinAdded(valueJoin);
				return;
			}
		}
		addJoin(valueJoin);
	}

	protected void joinAdded(FromItem valueJoin) {
		valueJoin.setParentNode(this);
	}

	public FromItem getFromItem(String alias) {
		if (this.alias.equals(alias))
			return this;
		for (FromItem join : joins) {
			FromItem result = join.getFromItem(alias);
			if (result != null)
				return result;
		}
		return null;
	}

	public FromItem getFromItemNotInUnion(String alias) {
		if (this.alias.equals(alias))
			return this;
		for (FromItem join : joins) {
			FromItem result = join.getFromItemNotInUnion(alias);
			if (result != null)
				return result;
		}
		return null;
	}

	public void removeFilter(SqlExpr sqlExpr) {
		for (int i = filters.size() - 1; i >= 0; i--) {
			if (filters.get(i) == sqlExpr) {
				filters.remove(i);
				break;
			}
		}
	}

	public List<ColumnVar> appendVars(List<ColumnVar> vars) {
		for (FromItem join : joins) {
			join.appendVars(vars);
		}
		return vars;
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		if (left) {
			sb.append("LEFT ");
		}
		sb.append(super.getSignature());
		sb.append(" ").append(alias);
		return sb.toString();
	}

	@Override
	public FromItem clone() {
		FromItem clone = (FromItem)super.clone();
		clone.joins = new ArrayList<FromItem>();
		for (FromItem join : joins) {
			clone.addJoin(join.clone());
		}
		clone.filters = new ArrayList<SqlExpr>();
		for (SqlExpr expr : filters) {
			clone.addFilter(expr.clone());
		}
		return clone;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		for (FromItem join : new ArrayList<FromItem>(joins)) {
			join.visit(visitor);
		}
		for (SqlExpr expr : new ArrayList<SqlExpr>(filters)) {
			expr.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = joins.size(); i < n; i++) {
			if (current == joins.get(i)) {
				joins.set(i, (FromItem)replacement);
				joinAdded((FromItem)replacement);
				return;
			}
		}
		for (int i = 0, n = filters.size(); i < n; i++) {
			if (current == filters.get(i)) {
				filters.set(i, (SqlExpr)replacement);
				replacement.setParentNode(this);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

}
