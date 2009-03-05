/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelNodeBase;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;

/**
 * A particular column in an SQL expression.
 * 
 * @author James Leigh
 */
public class IdColumn extends RdbmsQueryModelNodeBase implements SqlExpr {

	private String alias;

	private String column;

	public IdColumn(String alias) {
		super();
		this.alias = alias;
		this.column = "id";
	}

	public IdColumn(ColumnVar var) {
		super();
		this.alias = var.getAlias();
		this.column = var.getColumn();
	}

	public IdColumn(String alias, String column) {
		super();
		this.alias = alias;
		this.column = column;
	}

	public String getAlias() {
		return alias;
	}

	public String getColumn() {
		return column;
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " " + alias + "." + column;
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public IdColumn clone() {
		return (IdColumn)super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + ((column == null) ? 0 : column.hashCode());
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
		final IdColumn other = (IdColumn)obj;
		if (alias == null) {
			if (other.alias != null) {
				return false;
			}
		}
		else if (!alias.equals(other.alias)) {
			return false;
		}
		if (column == null) {
			if (other.column != null) {
				return false;
			}
		}
		else if (!column.equals(other.column)) {
			return false;
		}
		return true;
	}
}
