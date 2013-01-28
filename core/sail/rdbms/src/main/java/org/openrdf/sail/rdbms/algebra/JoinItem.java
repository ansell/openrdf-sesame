/* 
 * Licensed to Aduna under one or more contributor license agreements.  
 * See the NOTICE.txt file distributed with this work for additional 
 * information regarding copyright ownership. 
 *
 * Aduna licenses this file to you under the terms of the Aduna BSD 
 * License (the "License"); you may not use this file except in compliance 
 * with the License. See the LICENSE.txt file distributed with this work 
 * for the full License.
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing permissions
 * and limitations under the License.
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

	private Number predId;

	private List<ColumnVar> vars = new ArrayList<ColumnVar>();

	public JoinItem(String alias, String tableName, Number predId) {
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

	public Number getPredId() {
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
		JoinItem clone = (JoinItem)super.clone();
		clone.vars = new ArrayList<ColumnVar>(vars);
		return clone;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

}
