/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.query.algebra.TupleExpr;
import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelNodeBase;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlConstant;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;

/**
 * An SQL query.
 * 
 * @author James Leigh
 * 
 */
public class SelectQuery extends RdbmsQueryModelNodeBase implements TupleExpr {

	public static class OrderElem {

		public final SqlExpr sqlExpr;

		public final boolean isAscending;

		protected OrderElem(SqlExpr sqlExpr, boolean isAscending) {
			this.sqlExpr = sqlExpr;
			this.isAscending = isAscending;
		}
	}

	private Map<String, SelectProjection> projections = new HashMap<String, SelectProjection>();

	private Map<String, String> bindingVars;

	private boolean distinct;

	private FromItem from;

	private List<OrderElem> order = new ArrayList<OrderElem>();

	private Long offset;

	private Long limit;

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct(boolean b) {
		distinct = b;
	}

	public boolean isComplex() {
		if (offset != null || limit != null)
			return true;
		return isDistinct() || !order.isEmpty();
	}

	public FromItem getFrom() {
		return from;
	}

	public void setFrom(FromItem from) {
		this.from = from;
		from.setParentNode(this);
	}

	public List<OrderElem> getOrderElems() {
		return order;
	}

	public void addOrder(SqlExpr order, boolean isAscending) {
		if (order instanceof SqlNull)
			return;
		if (order instanceof SqlConstant<?>)
			return;
		this.order.add(new OrderElem(order, isAscending));
		order.setParentNode(this);
	}

	public Long getOffset() {
		return offset;
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	public Long getLimit() {
		return limit;
	}

	public void setLimit(Long limit) {
		this.limit = limit;
	}

	public Collection<String> getBindingNames(ColumnVar var) {
		if (bindingVars == null)
			return Collections.singleton(var.getName());
		List<String> list = new ArrayList<String>(bindingVars.size());
		for (String name : bindingVars.keySet()) {
			if (var.getName().equals(bindingVars.get(name))) {
				list.add(name);
			}
		}
		return list;
	}

	public Set<String> getBindingNames() {
		if (bindingVars == null) {
			Set<String> names = new HashSet<String>();
			for (ColumnVar var : getVars()) {
				names.add(var.getName());
			}
			return names;
		}
		return new HashSet<String>(bindingVars.keySet());
	}
	
	public Set<String> getAssuredBindingNames() {
		// FIXME: implement this properly
		return Collections.emptySet();
	}

	public void setBindingVars(Map<String, String> bindingVars) {
		this.bindingVars = bindingVars;
	}

	public Collection<SelectProjection> getSqlSelectVar() {
		return projections.values();
	}

	public void setSqlSelectVar(Collection<SelectProjection> projections) {
		this.projections.clear();
		for (SelectProjection p : projections) {
			addSqlSelectVar(p);
		}
	}

	public SelectProjection getSelectProjection(String name) {
		return projections.get(name);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (String name : projections.keySet()) {
			if (projections.get(name) == current) {
				projections.put(name, (SelectProjection)replacement);
				replacement.setParentNode(this);
				return;
			}
		}
		if (from == current) {
			from = (FromItem)replacement;
			replacement.setParentNode(this);
			return;
		}
		for (int i = 0, n = order.size(); i < n; i++) {
			if (order.get(i).sqlExpr == current) {
				if (replacement instanceof SqlNull || order instanceof SqlConstant<?>) {
					order.remove(i);
					return;
				}
				boolean asc = order.get(i).isAscending;
				order.set(i, new OrderElem((SqlExpr)replacement, asc));
				replacement.setParentNode(this);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		from.visit(visitor);
		ArrayList<SelectProjection> list = new ArrayList<SelectProjection>(projections.values());
		for (SelectProjection expr : list) {
			expr.visit(visitor);
		}
		for (OrderElem by : new ArrayList<OrderElem>(order)) {
			by.sqlExpr.visit(visitor);
		}
	}

	@Override
	public SelectQuery clone() {
		SelectQuery clone = (SelectQuery)super.clone();
		clone.distinct = distinct;
		clone.projections = new HashMap<String, SelectProjection>();
		for (SelectProjection expr : projections.values()) {
			clone.addSqlSelectVar(expr.clone());
		}
		clone.from = from.clone();
		clone.order = new ArrayList<OrderElem>(order);
		return clone;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	public boolean hasSqlSelectVar(SelectProjection node) {
		return projections.containsKey(node.getVar().getName());
	}

	public boolean hasSqlSelectVarName(String name) {
		return projections.containsKey(name);
	}

	public void addSqlSelectVar(SelectProjection node) {
		projections.put(node.getVar().getName(), node);
		node.setParentNode(this);
	}

	public Collection<ColumnVar> getProjections() {
		List<ColumnVar> vars = new ArrayList<ColumnVar>();
		for (SelectProjection proj : projections.values()) {
			ColumnVar var = proj.getVar();
			if (bindingVars == null) {
				vars.add(var);
			} else {
				for (String name : bindingVars.keySet()) {
					if (var.getName().equals(bindingVars.get(name))) {
						vars.add(var.as(name));
					}
				}
			}
		}
		return vars;
	}

	public Collection<ColumnVar> getVars() {
		List<ColumnVar> vars = new ArrayList<ColumnVar>();
		from.appendVars(vars);
		return vars;
	}

	public ColumnVar getVar(String varName) {
		return from.getVar(varName);
	}

	public void addFilter(SqlExpr sql) {
		from.addFilter(sql);
	}

	public void addJoin(SelectQuery right) {
		from.addJoin(right.getFrom());
	}

	public void addLeftJoin(SelectQuery right) {
		FromItem join = right.getFrom();
		join.setLeft(true);
		from.addJoin(join);
	}

	public FromItem getFromItem(String alias) {
		return from.getFromItemNotInUnion(alias);
	}

	public List<SqlExpr> getFilters() {
		return from.getFilters();
	}

	public void removeFilter(SqlExpr sqlExpr) {
		from.removeFilter(sqlExpr);
	}

	public Map<String, ColumnVar> getVarMap() {
		Collection<ColumnVar> vars = getVars();
		Map<String, ColumnVar> map = new HashMap<String, ColumnVar>(vars.size());
		for (ColumnVar var : vars) {
			if (!map.containsKey(var.getName())) {
				map.put(var.getName(), var);
			}
		}
		return map;
	}

}
