/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2006.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract superclass for N-ary value operators.
 * 
 * @author Jeen
 */
public abstract class NAryValueOperator extends QueryModelNodeBase implements ValueExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	protected List<ValueExpr> args;

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NAryValueOperator() {
	}

	/**
	 * Creates a new N-Ary value operator.
	 * 
	 * @param args
	 *        The operator's list of arguments, must not be <tt>null</tt>.
	 */
	public NAryValueOperator(List<ValueExpr> args) {
		setArguments(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setArguments(List<ValueExpr> args) {
		this.args = args;
	}
	
	public List<ValueExpr> getArguments() {
		return this.args;
	}
	
	public void addArgument(ValueExpr arg) {
		if (args == null) {
			args = new ArrayList<ValueExpr>();
		}
		args.add(arg);
		arg.setParentNode(this);
	}
	
	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for(ValueExpr arg: args) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		
		boolean replaced = false;
		
		for (int i =0 ; i < args.size(); i++ ) {
			ValueExpr arg = args.get(i);
			if (arg == current) {
				args.remove(i);
				args.add(i, (ValueExpr)replacement);
				replaced = true;
			}
		}
		
		if (!replaced) {
			super.replaceChildNode(current, replacement);
		}
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof NAryValueOperator) {
			NAryValueOperator o = (NAryValueOperator)other;
			
			return getArguments().equals(o.getArguments());
		}

		return false;
	}

	@Override
	public int hashCode() {
		return getArguments().hashCode();
	}

	@Override
	public NAryValueOperator clone() {
		NAryValueOperator clone = (NAryValueOperator)super.clone();
		
		clone.setArguments(new ArrayList<ValueExpr>());
		
		for(ValueExpr arg: getArguments()) {
			clone.addArgument(arg.clone());
		}
		
		return clone;
	}
}
