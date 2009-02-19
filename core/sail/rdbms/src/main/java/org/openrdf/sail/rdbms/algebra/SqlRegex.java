/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.sail.rdbms.algebra;

import org.openrdf.query.algebra.QueryModelVisitor;
import org.openrdf.sail.rdbms.algebra.base.BinarySqlOperator;
import org.openrdf.sail.rdbms.algebra.base.RdbmsQueryModelVisitorBase;
import org.openrdf.sail.rdbms.algebra.base.SqlExpr;

/**
 * The regular SQL expression - notation varies between databases.
 * 
 * @author James Leigh
 * 
 */
public class SqlRegex extends BinarySqlOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private SqlExpr flagsArg;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public SqlRegex() {
	}

	public SqlRegex(SqlExpr expr, SqlExpr pattern) {
		super(expr, pattern);
	}

	public SqlRegex(SqlExpr expr, SqlExpr pattern, SqlExpr flags) {
		super(expr, pattern);
		setFlagsArg(flags);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public SqlExpr getArg() {
		return super.getLeftArg();
	}

	public void setArg(SqlExpr leftArg) {
		super.setLeftArg(leftArg);
	}

	public SqlExpr getPatternArg() {
		return super.getRightArg();
	}

	public void setPatternArg(SqlExpr rightArg) {
		super.setRightArg(rightArg);
	}

	public void setFlagsArg(SqlExpr flags) {
		this.flagsArg = flags;
	}

	public SqlExpr getFlagsArg() {
		return flagsArg;
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		super.visitChildren(visitor);
		if (flagsArg != null) {
			flagsArg.visit(visitor);
		}
	}

	@Override
	public SqlRegex clone() {
		SqlRegex clone = (SqlRegex)super.clone();
		if (flagsArg != null) {
			clone.setFlagsArg(flagsArg.clone());
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
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((flagsArg == null) ? 0 : flagsArg.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		final SqlRegex other = (SqlRegex)obj;
		if (flagsArg == null) {
			if (other.flagsArg != null)
				return false;
		}
		else if (!flagsArg.equals(other.flagsArg))
			return false;
		return true;
	}
}
