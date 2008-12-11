/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The SLICE operator, as defined in <a
 * href="http://www.w3.org/TR/rdf-sparql-query/#defn_algSlice">SPARQL Query
 * Language for RDF</a>. The SLICE operator selects specific results from the
 * underlying tuple expression based onan offset and limit value (both
 * optional).
 * 
 * @author Arjohn Kampman
 */
public class Slice extends UnaryTupleOperator {

	private static final long serialVersionUID = -2457327353149198244L;

	/*-----------*
	 * Variables *
	 *-----------*/

	private int offset;

	private int limit;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Slice() {
	}

	public Slice(TupleExpr arg) {
		this(arg, 0, -1);
	}

	public Slice(TupleExpr arg, int offset, int limit) {
		super(arg);
		setOffset(offset);
		setLimit(limit);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	/**
	 * Checks whether the row selection has a (valid) offset.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt; 0</tt>
	 */
	public boolean hasOffset() {
		return offset > 0;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	/**
	 * Checks whether the row selection has a (valid) limit.
	 * 
	 * @return <tt>true</tt> when <tt>offset &gt;= 0</tt>
	 */
	public boolean hasLimit() {
		return limit >= 0;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature()
	{
		StringBuilder sb = new StringBuilder(256);

		sb.append(super.getSignature());
		sb.append(" ( ");

		if (hasLimit()) {
			sb.append("limit=").append(getLimit());
		}
		if (hasOffset()) {
			sb.append("offset=").append(getOffset());
		}

		sb.append(" )");

		return sb.toString();
	}

	@Override
	public Slice clone() {
		return (Slice)super.clone();
	}
}
