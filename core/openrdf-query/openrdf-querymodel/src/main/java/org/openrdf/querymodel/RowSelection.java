/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.querymodel;

import java.util.Set;

/**
 * Selects specific "rows" from the underlying tuple expression based on an
 * offset and limit value (both optional).
 */
public class RowSelection extends UnaryTupleOperator {

	/*-----------*
	 * Variables *
	 *-----------*/

	private int _offset;

	private int _limit;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public RowSelection(TupleExpr arg) {
		this(arg, 0, -1);
	}

	public RowSelection(TupleExpr arg, int offset, int limit) {
		super(arg);
		setOffset(offset);
		setLimit(limit);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public int getOffset() {
		return _offset;
	}

	public void setOffset(int offset) {
		_offset = offset;
	}

	/**
	 * Checks whether the row selection has a (valid) offset.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt; 0</tt>
	 */
	public boolean hasOffset() {
		return _offset > 0;
	}

	public int getLimit() {
		return _limit;
	}

	public void setLimit(int limit) {
		_limit = limit;
	}

	/**
	 * Checks whether the row selection has a (valid) limit.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt;= 0</tt>
	 */
	public boolean hasLimit() {
		return _limit >= 0;
	}

	public Set<String> getBindingNames() {
		return getArg().getBindingNames();
	}

	public void visit(QueryModelVisitor visitor) {
		visitor.meet(this);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(256);

		sb.append("ROW_SELECTION ( ");
		
		if (hasLimit()) {
			sb.append("limit=").append(getLimit());
		}
		if (hasOffset()) {
			sb.append("offset=").append(getOffset());
		}

		sb.append(" )");

		return sb.toString();
	}
}
