/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

/**
 * A BNode generator, which generates a new BNode each time it needs to supply a
 * value.
 */
public class BNodeGenerator extends QueryModelNodeBase implements ValueExpr {

	private ValueExpr nodeIdExpr = null;
	
	/*--------------*
	 * Constructors *
	 *--------------*/

	public BNodeGenerator() {
		super();
	}
	public BNodeGenerator(ValueExpr nodeIdExpr) {
		super();
		setNodeIdExpr(nodeIdExpr);
	}
	/*---------*
	 * Methods *
	 *---------*/

	public ValueExpr getNodeIdExpr() {
		return nodeIdExpr;
	}
	
	public void setNodeIdExpr(ValueExpr nodeIdExpr) {
		this.nodeIdExpr = nodeIdExpr;
	}
	
	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BNodeGenerator;
	}

	@Override
	public int hashCode() {
		return "BNodeGenerator".hashCode();
	}

	@Override
	public BNodeGenerator clone() {
		return (BNodeGenerator)super.clone();
	}
}
