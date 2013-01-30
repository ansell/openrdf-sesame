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
package org.openrdf.sail.rdbms.algebra.base;

import org.openrdf.query.algebra.Var;
import org.openrdf.sail.rdbms.algebra.ColumnVar;

/**
 * A column in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public abstract class ValueColumnBase extends RdbmsQueryModelNodeBase implements SqlExpr {

	private String name;

	private ColumnVar var;

	public ValueColumnBase(Var var) {
		this.name = var.getName();
	}

	public ValueColumnBase(ColumnVar var) {
		this.name = var.getName();
		setRdbmsVar(var);
	}

	public String getVarName() {
		return name;
	}

	public ColumnVar getRdbmsVar() {
		return var;
	}

	public void setRdbmsVar(ColumnVar var) {
		assert var != null;
		this.var = var;
	}

	public String getAlias() {
		return var.getAlias();
	}

	public String getColumn() {
		return var.getColumn();
	}

	@Override
	public String getSignature() {
		if (var != null)
			return super.getSignature() + " " + var;
		return super.getSignature() + " " + name;
	}

	@Override
	public ValueColumnBase clone() {
		return (ValueColumnBase)super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		final ValueColumnBase other = (ValueColumnBase)obj;
		if (name == null) {
			if (other.name != null)
				return false;
		}
		else if (!name.equals(other.name))
			return false;
		return true;
	}

}
