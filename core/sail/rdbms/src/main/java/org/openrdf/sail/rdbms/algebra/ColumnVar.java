/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.algebra.Var;
import org.openrdf.sail.rdbms.model.RdbmsResource;
import org.openrdf.sail.rdbms.model.RdbmsURI;
import org.openrdf.sail.rdbms.schema.ValueTypes;

/**
 * Represents a variable in an SQL expression.
 * 
 * @author James Leigh
 * 
 */
public class ColumnVar implements Cloneable {

	private int index;

	private boolean anonymous;

	private boolean hidden;

	private boolean implied;

	private String name;

	private Value value;

	private String alias;

	private String column;

	private boolean nullable;

	private ValueTypes types;

	private ColumnVar() {
	}

	public static ColumnVar createSubj(String alias, Var v, Resource resource) {
		ColumnVar var = new ColumnVar();
		var.alias = alias;
		var.column = "subj";
		var.name = v.getName();
		var.anonymous = v.isAnonymous();
		var.value = resource;
		var.types = ValueTypes.RESOURCE;
		if (resource instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		return var;
	}

	public static ColumnVar createPred(String alias, Var v, URI uri, boolean implied) {
		ColumnVar var = createSubj(alias, v, uri);
		var.column = "pred";
		var.implied = uri != null && implied;
		var.types = ValueTypes.URI;
		return var;
	}

	public static ColumnVar createObj(String alias, Var v, Value value) {
		ColumnVar var = new ColumnVar();
		var.alias = alias;
		var.column = "obj";
		var.name = v.getName();
		var.anonymous = v.isAnonymous();
		var.value = value;
		var.types = ValueTypes.UNKNOWN;
		if (value instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		else if (value instanceof RdbmsResource) {
			var.types = ValueTypes.RESOURCE;
		}
		return var;
	}

	public static ColumnVar createCtx(String alias, Var v, Resource resource) {
		ColumnVar var = new ColumnVar();
		var.alias = alias;
		var.column = "ctx";
		if (v == null) {
			var.name = "__ctx" + Integer.toHexString(System.identityHashCode(var));
			var.anonymous = true;
			var.hidden = true;
		}
		else {
			var.name = v.getName();
			var.anonymous = v.isAnonymous();
		}
		var.value = resource;
		var.types = ValueTypes.RESOURCE;
		if (resource instanceof RdbmsURI) {
			var.types = ValueTypes.URI;
		}
		return var;
	}

	public ValueTypes getTypes() {
		return types;
	}

	public void setTypes(ValueTypes types) {
		this.types = types;
	}

	public boolean isAnonymous() {
		return anonymous;
	}

	public boolean isHidden() {
		return hidden || value != null;
	}

	public boolean isImplied() {
		return implied;
	}

	public boolean isResource() {
		return !types.isLiterals();
	}

	public boolean isURI() {
		return !types.isLiterals() && !types.isBNodes();
	}

	public boolean isNullable() {
		return nullable;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public String getColumn() {
		return column;
	}

	public boolean isPredicate() {
		return "pred".equals(column);
	}

	public String getAlias() {
		return alias;
	}

	public ColumnVar as(String name) {
		try {
			ColumnVar clone = (ColumnVar)super.clone();
			clone.name = name;
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	public ColumnVar as(String alias, String column) {
		try {
			ColumnVar clone = (ColumnVar)super.clone();
			clone.alias = alias;
			clone.column = column;
			clone.nullable = true;
			return clone;
		}
		catch (CloneNotSupportedException e) {
			throw new AssertionError(e);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof ColumnVar) {
			return name.equals(((ColumnVar)other).name);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);

		sb.append(alias).append(".").append(column);

		sb.append(" (name=").append(name);

		if (value != null) {
			sb.append(", value=").append(value.toString());
		}

		sb.append(")");
		if (index > 0) {
			sb.append("#").append(index);
		}

		return sb.toString();
	}

}
