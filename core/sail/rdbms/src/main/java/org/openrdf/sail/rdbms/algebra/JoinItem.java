/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import java.util.ArrayList;
import java.util.List;

import org.openrdf.sail.rdbms.algebra.base.FromItem;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;

/**
 * An SQL join.
 * 
 * @author James Leigh
 * 
 */
public class JoinItem extends FromItem {
	private String tableName;
	private long predId;
	private List<ColumnVar> vars = new ArrayList<ColumnVar>();

	public JoinItem(String alias, String tableName, long predId) {
		super(alias);
		this.tableName = tableName;
		this.predId = predId;
	}

	public JoinItem(String alias, String tableName) {
		super(alias);
		this.tableName = tableName;
		this.predId = 0;
	}

	public String getTableName() {
		return tableName;
	}

	public long getPredId() {
		return predId;
	}

	public void addVar(ColumnVar var) {
		this.vars.add(var);
	}

	@Override
	public ColumnVar getVarForChildren(String name) {
		for (ColumnVar var : vars) {
			if (var.getName().equals(name))
				return var;
		}
		return super.getVarForChildren(name);
	}

	@Override
	public List<ColumnVar> appendVars(List<ColumnVar> vars) {
		vars.addAll(this.vars);
		return super.appendVars(vars);
	}

	@Override
	public String getSignature() {
		StringBuilder sb = new StringBuilder();
		if (isLeft()) {
			sb.append("LEFT ");
		}
		sb.append(super.getSignature());
		sb.append(" ").append(tableName);
		sb.append(" ").append(getAlias());
		return sb.toString();
	}

	@Override
	public JoinItem clone() {
		JoinItem clone = (JoinItem) super.clone();
		clone.vars = new ArrayList<ColumnVar>(vars);
		return clone;
	}

	@Override
	public <X extends Exception> void visit(
			RdbmsQueryModelVisitorBase<X> visitor) throws X {
		visitor.meet(this);
	}

}
