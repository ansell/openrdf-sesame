/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2011.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.parser;

import org.openrdf.query.algebra.TupleExpr;

/**
 * Abstract superclass of all operations that can be formulated in a query
 * language and parsed by the query parser.
 * 
 * @author Jeen Broekstra
 */
public abstract class ParsedOperation {

	private TupleExpr tupleExpr;
	
	/**
	 * @param tupleExpr
	 */
	public ParsedOperation(TupleExpr tupleExpr) {
		this.tupleExpr = tupleExpr;
	}

	/**
	 * 
	 */
	public ParsedOperation() {
		super();
	}
	
	/**
	 * Gets the tuple expression underlying this operation.
	 */
	public void setTupleExpr(TupleExpr tupleExpr) {
		assert tupleExpr != null : "tupleExpr must not be null";
		this.tupleExpr = tupleExpr;
	}

	/**
	 * Gets the tuple expression underlying this operation.
	 */
	public TupleExpr getTupleExpr() {
		return tupleExpr;
	}
	
	/**
	 * Returns a string representation of the operation that can be used for
	 * debugging.
	 */
	@Override
	public String toString()
	{
		return tupleExpr.toString();
	}

}
