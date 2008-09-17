/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2008.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.query.algebra;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * An abstract superclass for n-ary tuple operators which have one or more
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
	 * Creates a new n-ary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryTupleOperator(TupleExpr... args) {
		setArgs(args);
	}

	/**
	 * Creates a new n-ary tuple operator.
	 * 
	 * @param args
	 *        The operator's arguments, must not be <tt>null</tt>.
	 */
	public NaryTupleOperator(Collection<? extends TupleExpr> args) {
		setArgs(args.toArray(new TupleExpr[args.size()]));
	}

	/*---------*
	 * Methods *
	 *---------*/

	/**
	 * Gets the arguments of this n-ary tuple operator.
	 * 
	 * @return The operator's arguments.
	 */
	public TupleExpr[] getArgs() {
		return args.toArray(new TupleExpr[args.size()]);
	}

	/**
	 * Sets the arguments of this n-ary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void setArgs(TupleExpr... args) {
		this.args.clear();
		addArgs(args);
	}

	/**
	 * Adds arguments to this n-ary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void addArgs(TupleExpr... args) {
		assert args != null;
		for (TupleExpr arg : args) {
			addArg(arg);
		}
	}

	/**
	 * Adds an argument to this n-ary tuple operator.
	 * 
	 * @param args
	 *        The (new) arguments for this operator, must not be <tt>null</tt>.
	 */
	public void addArg(TupleExpr arg) {
		assert arg != null : "arg must not be null";
		arg.setParentNode(this);
		this.args.add(arg);
	}

	/**
	 * Gets the number of arguments of this n-ary tuple operator.
	 * 
	 * @return The number of arguments.
	 */
	public int getNumberOfArguments() {
		return args.size();
	}

	/**
	 * Gets the <tt>idx</tt>-th argument of this n-ary tuple operator.
	 * 
	 * @return The operator's arguments.
	 */
	public TupleExpr getArg(int idx) {
		return args.get(idx);
	}

	/**
	 * Sets the <tt>idx</tt>-th argument of this n-ary tuple operator.
	 * 
	 * @param arg
	 *        The (new) <tt>idx</tt>-th argument for this operator, must not be
	 *        <tt>null</tt>.
	 */
	public void setArg(int idx, TupleExpr arg) {
		assert arg != null : "arg must not be null";
		while (args.size() <= idx) {
			args.add(null);
		}
		arg.setParentNode(this);
		this.args.set(idx, arg);
	}

	/**
	 * Gets the only argument of this n-ary tuple operator.
	 * 
	 * @return The operator's argument.
	 */
	public TupleExpr getArg() {
		assert args.size() == 1;
		return args.get(0);
	}

	/**
	 * Sets the only argument of this n-ary tuple operator.
	 * 
	 * @param arg
	 *        The (new) argument for this operator, must not be <tt>null</tt>.
	 */
	public void setArg(TupleExpr arg) {
		assert arg != null : "arg must not be null";
		if (args.isEmpty()) {
			args.add(null);
		}
		assert args.size() == 1;
		arg.setParentNode(this);
		this.args.set(0, arg);
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
		int index = args.indexOf(current);
		if (index >= 0) {
			args.set(index, (TupleExpr)replacement);
			replacement.setParentNode(this);
		}
		else {
			super.replaceChildNode(current, replacement);
		}
	}

	public boolean removeArg(TupleExpr arg) {
		assert arg != null;
		return args.remove(arg);
	}

	@Override
	public NaryTupleOperator clone() {
		NaryTupleOperator clone = (NaryTupleOperator)super.clone();

		clone.args = new ArrayList<TupleExpr>(args.size());
		for (TupleExpr arg : args) {
			clone.addArg(arg.clone());
		}

		return clone;
	}
}
