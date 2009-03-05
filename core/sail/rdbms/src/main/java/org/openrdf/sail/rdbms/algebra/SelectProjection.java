/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.query.algebra.QueryModelNode;
import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelNodeBase;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;

/**
 * A collection of SQL expressions that form an RDF value binding.
 * 
 * @author James Leigh
 */
public class SelectProjection extends RdbmsQueryModelNodeBase {

	private ColumnVar var;

	private RefIdColumn id;

	private SqlExpr stringValue;

	private SqlExpr datatype;

	private SqlExpr language;

	public ColumnVar getVar() {
		return var;
	}

	public void setVar(ColumnVar var) {
		this.var = var;
	}

	public RefIdColumn getId() {
		return id;
	}

	public void setId(RefIdColumn id) {
		this.id = id;
		id.setParentNode(this);
	}

	public SqlExpr getStringValue() {
		return stringValue;
	}

	public void setStringValue(SqlExpr stringValue) {
		this.stringValue = stringValue;
		stringValue.setParentNode(this);
	}

	public SqlExpr getDatatype() {
		return datatype;
	}

	public void setDatatype(SqlExpr datatype) {
		this.datatype = datatype;
		datatype.setParentNode(this);
	}

	public SqlExpr getLanguage() {
		return language;
	}

	public void setLanguage(SqlExpr language) {
		this.language = language;
		language.setParentNode(this);
	}

	@Override
	public <X extends Exception> void visit(RdbmsQueryModelVisitorBase<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		id.visit(visitor);
		stringValue.visit(visitor);
		datatype.visit(visitor);
		language.visit(visitor);
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		if (id == current) {
			setId((RefIdColumn)replacement);
		}
		else if (stringValue == current) {
			setStringValue((SqlExpr)replacement);
		}
		else if (datatype == current) {
			setDatatype((SqlExpr)replacement);
		}
		else if (language == current) {
			setLanguage((SqlExpr)replacement);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public SelectProjection clone() {
		SelectProjection clone = (SelectProjection)super.clone();
		clone.setId(getId().clone());
		clone.setStringValue(getStringValue().clone());
		clone.setDatatype(getDatatype().clone());
		clone.setLanguage(getLanguage().clone());
		return clone;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((datatype == null) ? 0 : datatype.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((stringValue == null) ? 0 : stringValue.hashCode());
		result = prime * result + ((var == null) ? 0 : var.hashCode());
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
		final SelectProjection other = (SelectProjection)obj;
		if (datatype == null) {
			if (other.datatype != null) {
				return false;
			}
		}
		else if (!datatype.equals(other.datatype)) {
			return false;
		}
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		}
		else if (!id.equals(other.id)) {
			return false;
		}
		if (language == null) {
			if (other.language != null) {
				return false;
			}
		}
		else if (!language.equals(other.language)) {
			return false;
		}
		if (stringValue == null) {
			if (other.stringValue != null) {
				return false;
			}
		}
		else if (!stringValue.equals(other.stringValue)) {
			return false;
		}
		if (var == null) {
			if (other.var != null) {
				return false;
			}
		}
		else if (!var.equals(other.var)) {
			return false;
		}
		return true;
	}

}
