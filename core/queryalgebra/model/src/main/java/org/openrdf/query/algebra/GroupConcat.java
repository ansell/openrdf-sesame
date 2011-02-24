/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2010.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * The GROUP_CONCAT operator as defined in http://www.w3.org/TR/sparql11-query/#aggregates
 * 
 * @author Jeen Broekstra
 */
public class GroupConcat extends AggregateOperatorBase {

	private ValueExpr separator;
	
	public GroupConcat(ValueExpr arg) {
		super(arg);
	}

	public GroupConcat(ValueExpr arg, boolean distinct) {
		super(arg, distinct);
	}
	
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof GroupConcat && super.equals(other);
	}

	@Override
	public int hashCode() {
		return super.hashCode() ^ "Group_Concat".hashCode();
	}

	@Override
	public GroupConcat clone() {
		return (GroupConcat)super.clone();
	}
	
	public ValueExpr getSeparator() {
		return separator;
	}
	
	public void setSeparator(ValueExpr separator) {
		this.separator = separator;
	}
}
