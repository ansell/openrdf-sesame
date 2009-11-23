/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 2007-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.List;

/**
 * A call to an (external) function that operates on zero or more arguments.
 * 
 * @author Arjohn Kampman
 */
public class FunctionCall extends NaryValueOperator implements ValueExpr {

	private static final long serialVersionUID = 6812901135136982600L;

	/*-----------*
	 * Variables *
	 *-----------*/

	protected String uri;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public FunctionCall() {
	}

	/**
	 * Creates a new function calls.
	 * 
	 * @param args
	 *        The function's arguments.
	 */
	public FunctionCall(String uri, ValueExpr... args) {
		super(args);
		setURI(uri);
	}

	public FunctionCall(String uri, List<ValueExpr> args) {
		super(args);
		setURI(uri);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public String getURI() {
		return uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public <X extends Exception> void visit(QueryModelVisitor<X> visitor)
		throws X
	{
		visitor.meet(this);
	}

	@Override
	public String getSignature() {
		return super.getSignature() + " ( " + uri + ")";
	}

	@Override
	public FunctionCall clone() {
		return (FunctionCall)super.clone();
	}
}
