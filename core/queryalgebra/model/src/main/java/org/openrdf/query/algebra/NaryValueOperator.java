/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An abstract superclass for binary value operators which have one or more
 * arguments.
 */
public abstract class NaryValueOperator extends QueryModelNodeBase implements ValueExpr {

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

	public NaryValueOperator() {
		args = new ArrayList<ValueExpr>();
	}

	/**
	 * Creates a new nary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryValueOperator(ValueExpr... args) {
		setArgs(args);
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the arguments of this nary tuple operator.
	 * 
	 * @return The operator's arguments.
	 */
	public ValueExpr[] getArgs() {
		return args.toArray(new ValueExpr[args.size()]);
	}

	/**
	 * Sets the arguments of this nary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void setArgs(ValueExpr... args) {
		assert args != null;
		assert args.length > 0;
		for (ValueExpr arg : args) {
			assert arg != null : "arg must not be null";
			arg.setParentNode(this);
		}
		this.args = Arrays.asList(args);
	}

	/**
	 * Gets the number of arguments of this nary tuple operator.
	 * 
	 * @return The number of arguments.
	 */
	public int getNumberOfArguments() {
		return args.size();
	}

	/**
	 * Gets the <tt>idx</tt>-th argument of this nary tuple operator.
	 * 
	 * @return The operator's arguments.
	 */
	public ValueExpr getArg(int idx) {
		return args.get(idx);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this nary tuple operator.
	 * 
	 * @param arg
	 *        The (new) <tt>idx</tt>-th argument for this operator, must not
	 *        be <tt>null</tt>.
	 */
	public void setArg(int idx, ValueExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		while (args.size() <= idx) {
			args.add(null);
		}
		this.args.set(idx, arg);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (ValueExpr arg : args) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			if (getArg(i) == current) {
				setArg(i, (ValueExpr)replacement);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

	@Override
	public NaryValueOperator clone() {
		NaryValueOperator clone = (NaryValueOperator)super.clone();
		clone.setArgs(getArgs());
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			clone.setArg(i, getArg(i).clone());
		}
		return clone;
	}
}
