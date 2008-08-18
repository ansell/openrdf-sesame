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
 * An abstract superclass for binary tuple operators which have one or more
 * arguments.
 */
public abstract class NaryTupleOperator extends QueryModelNodeBase implements TupleExpr {

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	protected List<TupleExpr> args = new ArrayList<TupleExpr>();

	/*--------------*
	 * Constructors *
	 *--------------*/

	public NaryTupleOperator() {
	}

	/**
	 * Creates a new nary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryTupleOperator(TupleExpr... args) {
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
	public TupleExpr[] getArgs() {
		return args.toArray(new TupleExpr[args.size()]);
	}

	/**
	 * Sets the arguments of this nary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void setArgs(TupleExpr... args) {
		assert args != null;
		assert args.length > 0;
		for (TupleExpr arg : args) {
			assert arg != null : "arg must not be null";
			arg.setParentNode(this);
		}
		this.args.clear();
		this.args.addAll(Arrays.asList(args));
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
	public TupleExpr getArg(int idx) {
		return args.get(idx);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this nary tuple operator.
	 * 
	 * @param arg
	 *        The (new) <tt>idx</tt>-th argument for this operator, must not
	 *        be <tt>null</tt>.
	 */
	public void setArg(int idx, TupleExpr arg) {
		assert arg != null : "arg must not be null";
		while (args.size() <= idx) {
			args.add(null);
		}
		arg.setParentNode(this);
		this.args.set(idx, arg);
	}

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (TupleExpr arg : args) {
			arg.visit(visitor);
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			if (getArg(i) == current) {
				setArg(i, (TupleExpr)replacement);
				return;
			}
		}
		super.replaceChildNode(current, replacement);
	}

	public void removeChildNode(QueryModelNode current) {
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			if (getArg(i) == current) {
				args.remove(i);
				return;
			}
		}
		super.replaceChildNode(current, null);
	}

	@Override
	public NaryTupleOperator clone() {
		NaryTupleOperator clone = (NaryTupleOperator)super.clone();
		clone.args = new ArrayList<TupleExpr>(args.size());
		for (int i = 0, n = getNumberOfArguments(); i < n; i++) {
			clone.setArg(i, getArg(i).clone());
		}
		return clone;
	}
}
