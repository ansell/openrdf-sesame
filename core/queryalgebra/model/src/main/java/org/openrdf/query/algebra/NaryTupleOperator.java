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

	private static final long serialVersionUID = 4703129201150946365L;

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The operator's arguments.
	 */
	private List<TupleExpr> args = new ArrayList<TupleExpr>();

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
		setArg(args.size(), arg);
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
		if (idx >= args.size())
			return null;
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

	@Override
	public <X extends Exception> void visitChildren(QueryModelVisitor<X> visitor)
		throws X
	{
		for (TupleExpr arg : args) {
			if (arg != null) {
				arg.visit(visitor);
			}
		}
	}

	@Override
	public void replaceChildNode(QueryModelNode current, QueryModelNode replacement) {
		int index = args.indexOf(current);
		if (index >= 0) {
			setArg(index, (TupleExpr)replacement);
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
			if (arg != null) {
				clone.addArg(arg.clone());
			}
		}

		return clone;
	}
}
