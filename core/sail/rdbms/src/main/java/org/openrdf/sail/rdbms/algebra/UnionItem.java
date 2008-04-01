/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.schema.ValueTypes;

/**
 * An SQL UNION expression between two {@link FromItem}s.
 * 
 * @author James Leigh
 * 
 */
public class UnionItem extends FromItem {

	private List<FromItem> union = new ArrayList<FromItem>();

	public UnionItem(String alias) {
		super(alias);
	}

	@Override
	public FromItem getFromItem(String alias) {
		for (FromItem from : union) {
			FromItem item = from.getFromItem(alias);
			if (item != null)
				return item;
		}
		return super.getFromItem(alias);
	}

	public List<String> getSelectVarNames() {
		List<ColumnVar> vars = new ArrayList<ColumnVar>();
		for (FromItem from : union) {
			from.appendVars(vars);
		}
		List<String> selectVars = new ArrayList<String>();
		for (ColumnVar var : vars) {
			if (var.isHidden())
				continue;
			if (!selectVars.contains(var.getName())) {
				selectVars.add(var.getName());
			}
		}
		return selectVars;
	}

	public List<ColumnVar> getSelectColumns() {
		List<ColumnVar> vars = new ArrayList<ColumnVar>();
		for (FromItem from : union) {
			from.appendVars(vars);
		}
		List<ColumnVar> columns = new ArrayList<ColumnVar>();
		Map<String, ColumnVar> selectVars = new HashMap<String, ColumnVar>();
		for (ColumnVar var : vars) {
			if (var.isHidden())
				continue;
			if (selectVars.containsKey(var.getName())) {
				ValueTypes types = selectVars.get(var.getName()).getTypes();
				types = types.clone().merge(var.getTypes());
				selectVars.get(var.getName()).setTypes(types);
			}
			else {
				String name = var.getAlias() + var.getColumn();
				ColumnVar as = var.as(getAlias(), name);
				columns.add(as);
				selectVars.put(var.getName(), as);
			}
		}
		return columns;
	}

	@Override
	public List<ColumnVar> appendVars(List<ColumnVar> columns) {
		columns.addAll(getSelectColumns());
		return super.appendVars(columns);
	}

	@Override
	public ColumnVar getVar(String name) {
		for (ColumnVar var : appendVars(new ArrayList<ColumnVar>())) {
			if (var.getName().equals(name)) {
				return var;
			}
		}
		return null;
	}

	@Override
	public ColumnVar getVarForChildren(String name) {
		for (FromItem join : union) {
			ColumnVar var = join.getVar(name);
			if (var != null)
				return var;
		}
		return super.getVarForChildren(name);
	}

	public void addUnion(FromItem from) {
		union.add(from);
		from.setParentNode(this);
	}

	public List<FromItem> getUnion() {
		return union;
	}

	@Override
	public UnionItem clone() {
		UnionItem clone = (UnionItem)super.clone();
		clone.union = new ArrayList<FromItem>();
		for (FromItem from : union) {
			clone.addUnion(from.clone());
		}
		return clone;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = union.size(); i < n; i++) {
			if (current == union.get(i)) {
				union.set(i, (FromItem)replacement);
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
		for (FromItem join : new ArrayList<FromItem>(union)) {
			join.visit(visitor);
		}
	}

}
