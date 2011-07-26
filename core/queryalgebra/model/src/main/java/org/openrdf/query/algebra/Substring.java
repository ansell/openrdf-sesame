/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The SUBSTR function, as defined in <a
 * href="http://www.w3.org/TR/sparql11-query/#func-substr">SPARQL Query Language
 * for RDF</a>.
 * 
 * @author Jeen Broekstra
 */
public class Substring extends UnaryValueOperator {

	private ValueExpr startIndex;

	private ValueExpr length;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public Substring() {
	}

	public Substring(ValueExpr arg) {
		super(arg);
	}

	public Substring(ValueExpr arg, ValueExpr startIndex) {
		super(arg);
		setStartIndex(startIndex);
	}

	public Substring(ValueExpr arg, ValueExpr startIndex, ValueExpr length) {
		super(arg);
		setStartIndex(startIndex);
		setLength(length);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof Substring && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "SUBSTR".hashCode();
	}

	@Override
	public Substring clone() {
		Substring clone = (Substring)super.clone();
		
		if (getStartIndex() != null) {
			clone.setStartIndex(getStartIndex().clone());
		}
		if (getLength() != null) {
			clone.setLength(getLength().clone());
		}
		return clone;
	}

	/**
	 * @param length
	 *        The length to set.
	 */
	public void setLength(ValueExpr length) {
		this.length = length;
	}

	/**
	 * @return Returns the length.
	 */
	public ValueExpr getLength() {
		return length;
	}

	/**
	 * @param startIndex
	 *        The startIndex to set.
	 */
	public void setStartIndex(ValueExpr startIndex) {
		this.startIndex = startIndex;
	}

	/**
	 * @return Returns the startIndex.
	 */
	public ValueExpr getStartIndex() {
		return startIndex;
	}
}
